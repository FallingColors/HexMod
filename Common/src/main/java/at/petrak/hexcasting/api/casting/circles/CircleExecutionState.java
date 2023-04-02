package at.petrak.hexcasting.api.casting.circles;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.utils.HexUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * See {@link BlockEntityAbstractImpetus}, this is what's stored in it
 */
public class CircleExecutionState {
    public static final String
        TAG_KNOWN_POSITIONS = "known_positions",
        TAG_CURRENT_POS = "current_pos",
        TAG_ENTERED_FROM = "entered_from",
        TAG_IMAGE = "image",
        TAG_CASTER = "caster";

    // Does contain the starting impetus
    public final Set<BlockPos> knownPositions;
    public BlockPos currentPos;
    public Direction enteredFrom;
    public CastingImage currentImage;

    public final AABB bounds;

    public @Nullable UUID caster;


    protected CircleExecutionState(Set<BlockPos> knownPositions, BlockPos currentPos, Direction enteredFrom,
        CastingImage currentImage, @Nullable UUID caster) {
        this.knownPositions = knownPositions;
        this.currentPos = currentPos;
        this.enteredFrom = enteredFrom;
        this.currentImage = currentImage;
        this.caster = caster;

        this.bounds = BlockEntityAbstractImpetus.getBounds(new ArrayList<>(this.knownPositions));
    }

    // Return null if the circle does not close.
    public static @Nullable CircleExecutionState createNew(BlockEntityAbstractImpetus impetus, @Nullable ServerPlayer caster) {
        var level = (ServerLevel) impetus.getLevel();

        if (level == null)
            return null;

        // Flood fill! Just like VCC all over again.
        // this contains tentative positions and directions entered from
        var todo = new Stack<Pair<Direction, BlockPos>>();
        todo.add(Pair.of(impetus.getStartDirection(), impetus.getBlockPos().relative(impetus.getStartDirection())));
        var seenPositions = new HashSet<BlockPos>();
        var seenGoodPositions = new ArrayList<BlockPos>();

        while (!todo.isEmpty()) {
            var pair = todo.pop();
            var enterDir = pair.getFirst();
            var herePos = pair.getSecond();

            if (seenPositions.add(herePos)) {
                // it's new
                var hereBs = level.getBlockState(herePos);
                if (!(hereBs.getBlock() instanceof ICircleComponent cmp)) {
                    continue;
                }

                if (!cmp.canEnterFromDirection(enterDir, herePos, hereBs, level)) {
                    continue;
                }

                seenGoodPositions.add(herePos);
                var outs = cmp.possibleExitDirections(herePos, hereBs, level);
                for (var out : outs) {
                    todo.add(Pair.of(out, herePos.relative(out)));
                }
            }
        }

        if (!seenPositions.contains(impetus.getBlockPos()) || seenGoodPositions.isEmpty()) {
            return null;
        }

        var start = seenGoodPositions.get(0);

        if (caster == null)
            return new CircleExecutionState(new HashSet<>(seenGoodPositions), start, impetus.getStartDirection(), new CastingImage(), null);
        else
            return new CircleExecutionState(new HashSet<>(seenGoodPositions), start, impetus.getStartDirection(), new CastingImage(), caster.getUUID());
    }

    public CompoundTag save() {
        var out = new CompoundTag();

        var knownTag = new ListTag();
        for (var bp : this.knownPositions) {
            knownTag.add(NbtUtils.writeBlockPos(bp));
        }
        out.put(TAG_KNOWN_POSITIONS, knownTag);

        out.put(TAG_CURRENT_POS, NbtUtils.writeBlockPos(this.currentPos));
        out.putByte(TAG_ENTERED_FROM, (byte) this.enteredFrom.ordinal());
        out.put(TAG_IMAGE, this.currentImage.serializeToNbt());

        if (this.caster != null)
            out.putUUID(TAG_CASTER, this.caster);

        return out;
    }

    public static CircleExecutionState load(CompoundTag nbt, ServerLevel world) {
        var knownPositions = new HashSet<BlockPos>();
        var knownTag = nbt.getList(TAG_KNOWN_POSITIONS, Tag.TAG_COMPOUND);
        for (var tag : knownTag) {
            knownPositions.add(NbtUtils.readBlockPos(HexUtils.downcast(tag, CompoundTag.TYPE)));
        }

        var currentPos = NbtUtils.readBlockPos(nbt.getCompound(TAG_CURRENT_POS));
        var enteredFrom = Direction.values()[nbt.getByte(TAG_ENTERED_FROM)];
        var image = CastingImage.loadFromNbt(nbt.getCompound(TAG_IMAGE), world);

        UUID caster = null;
        if (nbt.hasUUID(TAG_CASTER))
            caster = nbt.getUUID(TAG_CASTER);

        return new CircleExecutionState(knownPositions, currentPos, enteredFrom, image, caster);
    }

    /**
     * Update this, also mutates the impetus.
     * <p>
     * Returns whether to continue.
     */
    public boolean tick(BlockEntityAbstractImpetus impetus) {
        var world = (ServerLevel) impetus.getLevel();

        if (world == null)
            return true; // if the world is null, try again next tick.

        ServerPlayer caster = null;
        if (this.caster != null && world.getEntity(this.caster) instanceof ServerPlayer player)
            caster = player;

        var env = new CircleCastEnv(world, impetus.getBlockPos(), impetus.getStartDirection(), caster, this.bounds);

        var executorBlock = world.getBlockState(this.currentPos);
        if (!(executorBlock instanceof ICircleComponent executor)) {
            // TODO: notification of the error?
            return false;
        }

        boolean halt = false;
        var ctrl = executor.acceptControlFlow(this.currentImage, env, this.enteredFrom, this.currentPos,
            executorBlock, world);
        if (ctrl instanceof ICircleComponent.ControlFlow.Stop) {
            // acceptControlFlow should have already posted the error
            halt = true;
        } else if (ctrl instanceof ICircleComponent.ControlFlow.Continue cont) {
            Pair<BlockPos, Direction> found = null;

            for (var exit : cont.exits) {
                var there = world.getBlockState(exit.getFirst());
                if (there instanceof ICircleComponent cc
                    && cc.canEnterFromDirection(exit.getSecond(), exit.getFirst(), there, world)) {
                    if (found != null) {
                        // oh no!
                        impetus.postError(
                            Component.translatable("hexcasting.circles.many_exits",
                                Component.literal(this.currentPos.toShortString()).withStyle(ChatFormatting.RED)),
                            new ItemStack(Items.COMPASS));
                        halt = true;
                        break;
                    } else {
                        found = exit;
                    }
                }
            }

            if (found == null) {
                // will never enter here if there were too many because found will have been set
                impetus.postError(
                    Component.translatable("hexcasting.circles.no_exits",
                        Component.literal(this.currentPos.toShortString()).withStyle(ChatFormatting.RED)),
                    new ItemStack(Items.OAK_SIGN));
                halt = true;
            }
        }

        return !halt;
    }

}
