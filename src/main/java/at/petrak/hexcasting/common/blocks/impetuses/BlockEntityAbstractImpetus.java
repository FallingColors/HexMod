package at.petrak.hexcasting.common.blocks.impetuses;

import at.petrak.hexcasting.api.ParticleSpray;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.lib.HexCapabilities;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
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
        TAG_FOUND_ALL = "found_all";

    @Nullable
    private UUID activator = null;
    @Nullable
    private FrozenColorizer colorizer = null;
    @Nullable
    private BlockPos nextBlock = null;
    @Nullable
    private List<BlockPos> trackedBlocks = null;
    private boolean foundAll = false;

    public BlockEntityAbstractImpetus(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    protected void activateSpellCircle(ServerPlayer activator) {
        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), this.getTickSpeed());

        this.activator = activator.getUUID();
        this.nextBlock = this.getBlockPos().above();
        this.trackedBlocks = new ArrayList<>();
        var maybeCap = activator.getCapability(HexCapabilities.PREFERRED_COLORIZER).resolve();
        maybeCap.ifPresent(capPreferredColorizer -> this.colorizer = capPreferredColorizer.colorizer);

        this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(BlockAbstractImpetus.LIT, true));
        var pos = Vec3.atCenterOf(this.getBlockPos());
        this.level.playSound(null, pos.x, pos.y, pos.z, HexSounds.SPELL_CIRCLE_AMBIANCE.get(), SoundSource.BLOCKS, 1f,
            1f);
    }

    protected void stepCircle() {
        // haha which silly idiot would have done something like this
        if (this.activator == null || this.colorizer == null || this.nextBlock == null || this.trackedBlocks == null) {
            this.level.destroyBlock(this.getBlockPos(), true);
            return;
        }

        if (this.foundAll) {
            var player = this.getPlayer();
            if (player != null) {
                var bob = new StringBuilder("[");
                for (int i = 0; i < this.trackedBlocks.size(); i++) {
                    bob.append('(');
                    bob.append(this.trackedBlocks.get(i).toShortString());
                    bob.append(')');

                    if (i < this.trackedBlocks.size() - 1) {
                        bob.append(", ");
                    }
                }
                bob.append(']');
                player.sendMessage(new TextComponent(bob.toString()), Util.NIL_UUID);
            }
            this.stopCasting();
            return;
        }

        for (var pos : this.trackedBlocks) {
            if (!this.level.getBlockState(pos).is(HexBlocks.SLATE.get())) {
                this.errorEffect(Vec3.atBottomCenterOf(pos));
                this.stopCasting();
                return;
            }
        }

        var blockChecking = this.level.getBlockState(this.nextBlock);
        if (!blockChecking.is(HexBlocks.SLATE.get())) {
            BlockPos errorPos;
            if (this.trackedBlocks.isEmpty()) {
                // then we just activated the circle
                errorPos = this.getBlockPos().above();
            } else {
                errorPos = this.trackedBlocks.get(this.trackedBlocks.size() - 1);
            }
            this.errorEffect(Vec3.atBottomCenterOf(errorPos));
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
                if (mightBeOkThere && blockThere.is(HexBlocks.SLATE.get())) {
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

        this.successEffect(Vec3.atBottomCenterOf(this.trackedBlocks.get(this.trackedBlocks.size() - 1)));
        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), this.getTickSpeed());
    }

    protected void successEffect(Vec3 pos) {
        if (this.level instanceof ServerLevel serverLevel) {
            var spray = new ParticleSpray(pos, new Vec3(0, 1, 0), 0.1, Mth.PI / 4, 30);
            spray.sprayParticles(serverLevel, this.colorizer);
        }
        level.playSound(null, pos.x, pos.y, pos.z, HexSounds.SPELL_CIRCLE_FIND_BLOCK.get(), SoundSource.BLOCKS, 1f, 1f);
    }

    protected void errorEffect(Vec3 pos) {
        if (this.level instanceof ServerLevel serverLevel) {
            var spray = new ParticleSpray(pos, new Vec3(0, 1, 0), 0.1, Mth.PI / 4, 30);
            spray.sprayParticles(serverLevel, new FrozenColorizer(
                HexItems.DYE_COLORIZERS[DyeColor.RED.ordinal()].get(),
                this.activator));
        }
        level.playSound(null, pos.x, pos.y, pos.z, HexSounds.SPELL_CIRCLE_FAIL.get(), SoundSource.BLOCKS, 1f, 1f);
    }

    protected void stopCasting() {
        this.activator = null;
        this.nextBlock = null;
        this.trackedBlocks = null;
        this.foundAll = false;

        this.level.setBlockAndUpdate(this.getBlockPos(),
            this.getBlockState().setValue(BlockAbstractImpetus.LIT, false));

        if (this.level.isClientSide) {
            Minecraft.getInstance().getSoundManager().stop(HexSounds.SPELL_CIRCLE_AMBIANCE.getId(), null);
        }
    }

    @Nullable
    protected Player getPlayer() {
        return this.level.getPlayerByUUID(this.activator);
    }

    protected int getTickSpeed() {
        return 10;
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
    }

    private static final Direction[] HORIZONTAL_DIRS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
}
