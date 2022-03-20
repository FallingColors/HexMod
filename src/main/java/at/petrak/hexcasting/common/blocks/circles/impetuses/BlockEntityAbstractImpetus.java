package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.HexConfig;
import at.petrak.hexcasting.api.ParticleSpray;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate;
import at.petrak.hexcasting.common.casting.CastingContext;
import at.petrak.hexcasting.common.casting.CastingHarness;
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.lib.HexCapabilities;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BlockEntityAbstractImpetus extends BlockEntity {
    public static final String
        TAG_ACTIVATOR = "activator",
        TAG_COLORIZER = "colorizer",
        TAG_NEXT_BLOCK = "next_block",
        TAG_TRACKED_BLOCKS = "tracked_blocks",
        TAG_FOUND_ALL = "found_all",
        TAG_MANA = "mana";

    @Nullable
    private UUID activator = null;
    @Nullable
    private FrozenColorizer colorizer = null;
    @Nullable
    private BlockPos nextBlock = null;
    @Nullable
    private List<BlockPos> trackedBlocks = null;
    private boolean foundAll = false;
    private int mana = 0;

    public BlockEntityAbstractImpetus(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    abstract public boolean playerAlwaysInRange();

    protected void activateSpellCircle(ServerPlayer activator) {
        if (this.nextBlock != null) {
            return;
        }
        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), this.getTickSpeed());

        this.activator = activator.getUUID();
        this.nextBlock = this.getBlockPos().above();
        this.trackedBlocks = new ArrayList<>();
        var maybeCap = activator.getCapability(HexCapabilities.PREFERRED_COLORIZER).resolve();
        maybeCap.ifPresent(capPreferredColorizer -> this.colorizer = capPreferredColorizer.colorizer);

        this.level.setBlockAndUpdate(this.getBlockPos(),
            this.getBlockState().setValue(BlockAbstractImpetus.ENERGIZED, true));
    }

    protected void stepCircle() {
        this.setChanged();

        // haha which silly idiot would have done something like this
        if (this.activator == null || this.colorizer == null || this.nextBlock == null || this.trackedBlocks == null) {
            this.level.destroyBlock(this.getBlockPos(), true);
            return;
        }

        var possibleErrorPos = this.checkEverythingOk();
        if (possibleErrorPos != null) {
            this.errorEffect(possibleErrorPos);
            this.stopCasting();
            return;
        }

        if (this.foundAll) {
            this.castSpell();
            this.stopCasting();
            return;
        }

        // Awesome we know this block is OK
        if (this.trackedBlocks.isEmpty()) {
            // then this is the very first activation!
            this.trackedBlocks.add(this.nextBlock);
            // and shunt it along by one
            this.nextBlock = this.nextBlock.relative(this.getBlockState().getValue(BlockAbstractImpetus.FACING));
        } else {
            BlockPos foundPos = null;
            for (var dir : HORIZONTAL_DIRS) {
                var neighborPos = this.nextBlock.relative(dir);
                var blockThere = this.level.getBlockState(neighborPos);
                // at this point, we haven't actually added nextBlock to trackedBlocks
                // so, in the smallest circle case (a 2x2), this will have a size of 3 (with this block being the 4th).
                var closedLoop = (this.trackedBlocks.size() >= 3 && this.trackedBlocks.get(0).equals(neighborPos));
                var mightBeOkThere = closedLoop || !this.trackedBlocks.contains(neighborPos);
                if (mightBeOkThere && blockThere.is(HexBlocks.WRITTEN_SLATE.get())) {
                    if (foundPos == null) {
                        foundPos = neighborPos;
                        this.foundAll |= closedLoop;
                    } else {
                        // uh oh, fork in the road
                        this.errorEffect(Vec3.atBottomCenterOf(this.nextBlock));
                        this.stopCasting();
                        return;
                    }
                }
            }
            if (foundPos != null) {
                // pog
                this.trackedBlocks.add(this.nextBlock);
                this.nextBlock = foundPos;
            } else {
                // end of the line
                this.errorEffect(Vec3.atBottomCenterOf(this.nextBlock));
                this.stopCasting();
                return;
            }
        }

        var lastPos = this.trackedBlocks.get(this.trackedBlocks.size() - 1);
        var justTrackedBlock = this.level.getBlockState(lastPos);
        this.level.setBlockAndUpdate(lastPos, justTrackedBlock.setValue(BlockSlate.LIT, true));
        this.successEffect(Vec3.atBottomCenterOf(lastPos));

        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), this.getTickSpeed());
    }

    private void castSpell() {
        var player = this.getPlayer();

        if (player instanceof ServerPlayer splayer) {
            var ctx = new CastingContext(splayer, InteractionHand.MAIN_HAND);
            var harness = new CastingHarness(ctx);
            for (var tracked : this.trackedBlocks) {
                if (this.level.getBlockEntity(tracked) instanceof BlockEntitySlate slate) {
                    var info = harness.executeNewPattern(slate.pattern, splayer.getLevel());
                    if (info.getWasPrevPatternInvalid()) {
                        this.errorEffect(Vec3.atBottomCenterOf(tracked));
                        break;
                    }
                }
            }
        }

        this.level.playSound(null, this.getBlockPos(), HexSounds.SPELL_CIRCLE_CAST.get(), SoundSource.BLOCKS, 2f, 1f);
    }

    @Nullable
    private Vec3 checkEverythingOk() {
        // if they logged out or changed dimensions or something
        if (this.getPlayer() == null) {
            return Vec3.atBottomCenterOf(this.getBlockPos().above());
        }

        for (var pos : this.trackedBlocks) {
            if (!this.level.getBlockState(pos).is(HexBlocks.WRITTEN_SLATE.get())) {
                return Vec3.atBottomCenterOf(pos);
            }
        }

        var blockChecking = this.level.getBlockState(this.nextBlock);
        if (!blockChecking.is(HexBlocks.WRITTEN_SLATE.get())) {
            BlockPos errorPos;
            if (this.trackedBlocks.isEmpty()) {
                // then we just activated the circle
                errorPos = this.getBlockPos().above();
            } else {
                errorPos = this.trackedBlocks.get(this.trackedBlocks.size() - 1);
            }
            return Vec3.atBottomCenterOf(errorPos);
        }

        if (this.trackedBlocks.size() > HexConfig.maxSpellCircleLength.get()) {
            return Vec3.atBottomCenterOf(this.trackedBlocks.get(this.trackedBlocks.size() - 1));
        }

        return null;
    }

    protected void successEffect(Vec3 pos) {
        if (this.level instanceof ServerLevel serverLevel) {
            var spray = new ParticleSpray(pos, new Vec3(0, 1, 0), 0.1, Mth.PI / 4, 30);
            spray.sprayParticles(serverLevel, this.colorizer);
        }
        // This is a good use of my time
        var note = this.trackedBlocks.size() - 1;
        var semitone = this.semitoneFromScale(note);
        var pitch = Math.pow(2.0, (semitone - 8) / 12d);
        level.playSound(null, pos.x, pos.y, pos.z, HexSounds.SPELL_CIRCLE_FIND_BLOCK.get(), SoundSource.BLOCKS, 1f,
            (float) pitch);
    }

    protected void errorEffect(Vec3 pos) {
        if (this.level instanceof ServerLevel serverLevel) {
            var spray = new ParticleSpray(pos, new Vec3(0, 1.5, 0), 0.1, Mth.PI / 2, 40);
            spray.sprayParticles(serverLevel, new FrozenColorizer(
                HexItems.DYE_COLORIZERS[DyeColor.RED.ordinal()].get(),
                this.activator));
        }
        level.playSound(null, pos.x, pos.y, pos.z, HexSounds.SPELL_CIRCLE_FAIL.get(), SoundSource.BLOCKS, 1f, 1f);
    }

    protected void stopCasting() {
        for (var tracked : this.trackedBlocks) {
            var bs = this.level.getBlockState(tracked);
            this.level.setBlockAndUpdate(tracked, bs.setValue(BlockSlate.LIT, false));
        }

        this.activator = null;
        this.nextBlock = null;
        this.trackedBlocks = null;
        this.foundAll = false;

        this.level.setBlockAndUpdate(this.getBlockPos(),
            this.getBlockState().setValue(BlockAbstractImpetus.LIT, false));
    }

    @Nullable
    protected Player getPlayer() {
        return this.level.getPlayerByUUID(this.activator);
    }

    protected int getTickSpeed() {
        return 8;
    }

    protected int semitoneFromScale(int note) {
        var blockBelow = this.level.getBlockState(this.getBlockPos().below());
        var scale = MAJOR_SCALE;
        if (blockBelow.is(Blocks.CRYING_OBSIDIAN)) {
            scale = MINOR_SCALE;
        } else if (blockBelow.is(BlockTags.DOORS) || blockBelow.is(BlockTags.TRAPDOORS)) {
            scale = DORIAN_SCALE;
        } else if (blockBelow.is(Blocks.PISTON) || blockBelow.is(Blocks.STICKY_PISTON)) {
            scale = MIXOLYDIAN_SCALE;
        } else if (blockBelow.is(Blocks.BLUE_WOOL)
            || blockBelow.is(Blocks.BLUE_CONCRETE) || blockBelow.is(Blocks.BLUE_CONCRETE_POWDER)
            || blockBelow.is(Blocks.BLUE_TERRACOTTA) || blockBelow.is(Blocks.BLUE_GLAZED_TERRACOTTA)
            || blockBelow.is(Blocks.BLUE_STAINED_GLASS) || blockBelow.is(Blocks.BLUE_STAINED_GLASS_PANE)) {
            scale = BLUES_SCALE;
        } else if (blockBelow.is(Blocks.BONE_BLOCK)) {
            scale = BAD_TIME;
        } else if (blockBelow.is(Blocks.COMPOSTER)) {
            scale = SUSSY_BAKA;
        }

        note = Mth.clamp(note, 0, scale.length - 1);
        return scale[note];
    }


    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (this.activator != null && this.colorizer != null && this.nextBlock != null && this.trackedBlocks != null) {
            tag.putUUID(TAG_ACTIVATOR, this.activator);
            tag.put(TAG_NEXT_BLOCK, NbtUtils.writeBlockPos(this.nextBlock));
            tag.put(TAG_COLORIZER, this.colorizer.serialize());
            tag.putBoolean(TAG_FOUND_ALL, this.foundAll);

            var trackeds = new ListTag();
            for (var tracked : this.trackedBlocks) {
                trackeds.add(NbtUtils.writeBlockPos(tracked));
            }
            tag.put(TAG_TRACKED_BLOCKS, trackeds);
        }
        tag.putInt(TAG_MANA, this.mana);
    }

    @Override
    public CompoundTag getUpdateTag() {
        var out = new CompoundTag();
        this.saveAdditional(out);
        return out;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(TAG_ACTIVATOR) && tag.contains(TAG_COLORIZER) && tag.contains(TAG_NEXT_BLOCK)
            && tag.contains(TAG_TRACKED_BLOCKS)) {
            this.activator = tag.getUUID(TAG_ACTIVATOR);
            this.colorizer = FrozenColorizer.deserialize(tag.getCompound(TAG_COLORIZER));
            this.nextBlock = NbtUtils.readBlockPos(tag.getCompound(TAG_NEXT_BLOCK));
            this.foundAll = tag.getBoolean(TAG_FOUND_ALL);
            var trackeds = tag.getList(TAG_TRACKED_BLOCKS, Tag.TAG_COMPOUND);
            this.trackedBlocks = new ArrayList<>(trackeds.size());
            for (var tracked : trackeds) {
                this.trackedBlocks.add(NbtUtils.readBlockPos((CompoundTag) tracked));
            }
        }

        this.mana = tag.getInt(TAG_MANA);
    }


    private static final Direction[] HORIZONTAL_DIRS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    // this is a good use of my time
    private static final int[] MAJOR_SCALE = {0, 2, 4, 5, 7, 9, 11, 12};
    private static final int[] MINOR_SCALE = {0, 2, 3, 5, 7, 8, 11, 12};
    private static final int[] DORIAN_SCALE = {0, 2, 3, 5, 7, 9, 10, 12};
    private static final int[] MIXOLYDIAN_SCALE = {0, 2, 4, 5, 7, 9, 10, 12};
    private static final int[] BLUES_SCALE = {0, 3, 5, 6, 7, 10, 12};
    private static final int[] BAD_TIME = {0, 0, 12, 7, 6, 5, 3, 0, 3, 5};
    private static final int[] SUSSY_BAKA = {5, 8, 10, 11, 10, 8, 5, 3, 7, 5};
}
