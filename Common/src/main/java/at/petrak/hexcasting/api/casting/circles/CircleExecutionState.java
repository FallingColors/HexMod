package at.petrak.hexcasting.api.casting.circles;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.misc.Result;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
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
        TAG_IMPETUS_POS = "impetus_pos",
        TAG_IMPETUS_DIR = "impetus_dir",
        TAG_KNOWN_POSITIONS = "known_positions",
        TAG_REACHED_POSITIONS = "reached_positions",
        TAG_CURRENT_POS = "current_pos",
        TAG_ENTERED_FROM = "entered_from",
        TAG_IMAGE = "image",
        TAG_CASTER = "caster",
        TAG_PIGMENT = "pigment";

    public final BlockPos impetusPos;
    public final Direction impetusDir;
    // Does contain the starting impetus
    public final Set<BlockPos> knownPositions;
    public final List<BlockPos> reachedPositions;
    public BlockPos currentPos;
    public Direction enteredFrom;
    public CastingImage currentImage;
    public @Nullable UUID caster;
    public @Nullable FrozenPigment casterPigment;

    public final AABB bounds;


    protected CircleExecutionState(BlockPos impetusPos, Direction impetusDir, Set<BlockPos> knownPositions,
        List<BlockPos> reachedPositions, BlockPos currentPos, Direction enteredFrom,
        CastingImage currentImage, @Nullable UUID caster, @Nullable FrozenPigment casterPigment) {
        this.impetusPos = impetusPos;
        this.impetusDir = impetusDir;
        this.knownPositions = knownPositions;
        this.reachedPositions = reachedPositions;
        this.currentPos = currentPos;
        this.enteredFrom = enteredFrom;
        this.currentImage = currentImage;
        this.caster = caster;
        this.casterPigment = casterPigment;

        this.bounds = BlockEntityAbstractImpetus.getBounds(new ArrayList<>(this.knownPositions));
    }

    public @Nullable ServerPlayer getCaster(ServerLevel world) {
        if (this.caster == null) {
            return null;
        }
        var entity = world.getEntity(this.caster);
        if (entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        // there's a problem if this branch is reached
        return null;
    }

    // Return OK if it succeeded; returns Err if it didn't close and the location
    public static Result<CircleExecutionState, @Nullable BlockPos> createNew(BlockEntityAbstractImpetus impetus,
        @Nullable ServerPlayer caster) {
        var level = (ServerLevel) impetus.getLevel();

        if (level == null)
            return new Result.Err<>(null);

        // Flood fill! Just like VCC all over again.
        // this contains tentative positions and directions entered from
        var todo = new Stack<Pair<Direction, BlockPos>>();
        todo.add(Pair.of(impetus.getStartDirection(), impetus.getBlockPos().relative(impetus.getStartDirection())));
        var seenGoodPosSet = new HashSet<BlockPos>();
        var seenGoodPositions = new ArrayList<BlockPos>();

        while (!todo.isEmpty()) {
            var pair = todo.pop();
            var enterDir = pair.getFirst();
            var herePos = pair.getSecond();

            var hereBs = level.getBlockState(herePos);
            if (!(hereBs.getBlock() instanceof ICircleComponent cmp)) {
                continue;
            }
            if (!cmp.canEnterFromDirection(enterDir, herePos, hereBs, level)) {
                continue;
            }

            if (seenGoodPosSet.add(herePos)) {
                // it's new
                seenGoodPositions.add(herePos);
                var outs = cmp.possibleExitDirections(herePos, hereBs, level);
                for (var out : outs) {
                    todo.add(Pair.of(out, herePos.relative(out)));
                }
            }
        }

        if (seenGoodPositions.isEmpty()) {
            return new Result.Err<>(null);
        } else if (!seenGoodPosSet.contains(impetus.getBlockPos())) {
            // we can't enter from the side the directrix exits from, so this means we couldn't loop back.
            // the last item we tried to examine will always be a terminal slate (b/c if it wasn't,
            // then the *next* slate would be last qed)
            return new Result.Err<>(seenGoodPositions.get(seenGoodPositions.size() - 1));
        }

        var knownPositions = new HashSet<>(seenGoodPositions);
        var reachedPositions = new ArrayList<BlockPos>();
        reachedPositions.add(impetus.getBlockPos());
        var start = seenGoodPositions.get(0);

        FrozenPigment colorizer = null;
        UUID casterUUID;
        if (caster == null) {
            casterUUID = null;
        } else {
            colorizer = HexAPI.instance().getColorizer(caster);
            casterUUID = caster.getUUID();
        }
        return new Result.Ok<>(
            new CircleExecutionState(impetus.getBlockPos(), impetus.getStartDirection(), knownPositions,
                reachedPositions, start, impetus.getStartDirection(), new CastingImage(), casterUUID, colorizer));
    }

    public CompoundTag save() {
        var out = new CompoundTag();

        out.put(TAG_IMPETUS_POS, NbtUtils.writeBlockPos(this.impetusPos));
        out.putByte(TAG_IMPETUS_DIR, (byte) this.impetusDir.ordinal());

        var knownTag = new ListTag();
        for (var bp : this.knownPositions) {
            knownTag.add(NbtUtils.writeBlockPos(bp));
        }
        out.put(TAG_KNOWN_POSITIONS, knownTag);

        var reachedTag = new ListTag();
        for (var bp : this.reachedPositions) {
            reachedTag.add(NbtUtils.writeBlockPos(bp));
        }
        out.put(TAG_REACHED_POSITIONS, reachedTag);

        out.put(TAG_CURRENT_POS, NbtUtils.writeBlockPos(this.currentPos));
        out.putByte(TAG_ENTERED_FROM, (byte) this.enteredFrom.ordinal());
        out.put(TAG_IMAGE, this.currentImage.serializeToNbt());

        if (this.caster != null)
            out.putUUID(TAG_CASTER, this.caster);

        if (this.casterPigment != null)
            out.put(TAG_PIGMENT, this.casterPigment.serializeToNBT());

        return out;
    }

    public static CircleExecutionState load(CompoundTag nbt, ServerLevel world) {
        var startPos = NbtUtils.readBlockPos(nbt.getCompound(TAG_IMPETUS_POS));
        var startDir = Direction.values()[nbt.getByte(TAG_IMPETUS_DIR)];

        var knownPositions = new HashSet<BlockPos>();
        var knownTag = nbt.getList(TAG_KNOWN_POSITIONS, Tag.TAG_COMPOUND);
        for (var tag : knownTag) {
            knownPositions.add(NbtUtils.readBlockPos(HexUtils.downcast(tag, CompoundTag.TYPE)));
        }
        var reachedPositions = new ArrayList<BlockPos>();
        var reachedTag = nbt.getList(TAG_REACHED_POSITIONS, Tag.TAG_COMPOUND);
        for (var tag : reachedTag) {
            reachedPositions.add(NbtUtils.readBlockPos(HexUtils.downcast(tag, CompoundTag.TYPE)));
        }

        var currentPos = NbtUtils.readBlockPos(nbt.getCompound(TAG_CURRENT_POS));
        var enteredFrom = Direction.values()[nbt.getByte(TAG_ENTERED_FROM)];
        var image = CastingImage.loadFromNbt(nbt.getCompound(TAG_IMAGE), world);

        UUID caster = null;
        if (nbt.hasUUID(TAG_CASTER))
            caster = nbt.getUUID(TAG_CASTER);

        FrozenPigment pigment = null;
        if (nbt.contains(TAG_PIGMENT, Tag.TAG_COMPOUND))
            pigment = FrozenPigment.fromNBT(nbt.getCompound(TAG_PIGMENT));

        return new CircleExecutionState(startPos, startDir, knownPositions, reachedPositions, currentPos,
            enteredFrom, image, caster, pigment);
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

        var env = new CircleCastEnv(world, this);

        var executorBlockState = world.getBlockState(this.currentPos);
        if (!(executorBlockState.getBlock() instanceof ICircleComponent executor)) {
            // TODO: notification of the error?
            ICircleComponent.sfx(this.currentPos, executorBlockState, world,
                Objects.requireNonNull(env.getImpetus()), false);
            return false;
        }

        executorBlockState = executor.startEnergized(this.currentPos, executorBlockState, world);
        this.reachedPositions.add(this.currentPos);

        // Do the execution!
        boolean halt = false;
        var ctrl = executor.acceptControlFlow(this.currentImage, env, this.enteredFrom, this.currentPos,
            executorBlockState, world);

        if (env.getImpetus() == null)
            return false; //the impetus got removed during the cast and no longer exists in the world. stop casting

        if (ctrl instanceof ICircleComponent.ControlFlow.Stop) {
            // acceptControlFlow should have already posted the error
            halt = true;
        } else if (ctrl instanceof ICircleComponent.ControlFlow.Continue cont) {
            Pair<BlockPos, Direction> found = null;

            for (var exit : cont.exits) {
                var there = world.getBlockState(exit.getFirst());
                if (there.getBlock() instanceof ICircleComponent cc
                    && cc.canEnterFromDirection(exit.getSecond(), exit.getFirst(), there, world)) {
                    if (found != null) {
                        // oh no!
                        impetus.postDisplay(
                            Component.translatable("hexcasting.tooltip.circle.many_exits",
                                Component.literal(this.currentPos.toShortString()).withStyle(ChatFormatting.RED)),
                            new ItemStack(Items.COMPASS));
                        ICircleComponent.sfx(this.currentPos, executorBlockState, world,
                            Objects.requireNonNull(env.getImpetus()), false);
                        halt = true;
                        break;
                    } else {
                        found = exit;
                    }
                }
            }

            if (found == null) {
                // will never enter here if there were too many because found will have been set
                ICircleComponent.sfx(this.currentPos, executorBlockState, world,
                    Objects.requireNonNull(env.getImpetus()), false);
                impetus.postNoExits(this.currentPos);
                halt = true;
            } else {
                // A single valid exit position has been found.
                ICircleComponent.sfx(this.currentPos, executorBlockState, world,
                    Objects.requireNonNull(env.getImpetus()), true);
                currentPos = found.getFirst();
                enteredFrom = found.getSecond();
                currentImage = cont.update.withOverriddenUsedOps(0); // reset ops used after each slate finishes executing
            }
        }

        return !halt;
    }

    /**
     * How many ticks should pass between activations, given the number of blocks encountered so far.
     */
    protected int getTickSpeed() {
        return Math.max(2, 10 - (this.reachedPositions.size() - 1) / 3);
    }

    public void endExecution(BlockEntityAbstractImpetus impetus) {
        var world = (ServerLevel) impetus.getLevel();

        if (world == null)
            return; // TODO: error here?

        for (var pos : this.reachedPositions) {
            var there = world.getBlockState(pos);
            if (there.getBlock() instanceof ICircleComponent cc) {
                cc.endEnergized(pos, there, world);
            }
        }
    }
}
