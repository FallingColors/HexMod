package at.petrak.hexcasting.common.blocks.impetuses;

import at.petrak.hexcasting.api.ParticleSpray;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
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
        TAG_NEXT_BLOCK = "next_block",
        TAG_TRACKED_BLOCKS = "tracked_blocks";

    @Nullable
    private UUID activator = null;
    @Nullable
    private BlockPos nextBlock = null;
    @Nullable
    private List<BlockPos> trackedBlocks = null;

    public BlockEntityAbstractImpetus(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    protected void activateSpellCircle(Player activator) {
        this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), this.getTickSpeed());

        this.activator = activator.getUUID();
        this.nextBlock = this.getBlockPos().above();
        this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(BlockAbstractImpetus.LIT, true));

        activator.playSound(HexSounds.SPELL_CIRCLE_FAIL.get(), 1f, 1f);
    }

    protected void stepCircle() {
        for (var pos : this.trackedBlocks) {
            if (!this.level.getBlockState(pos).is(HexBlocks.SLATE.get())) {
                this.errorParticles(Vec3.atBottomCenterOf(pos));
                this.stopCasting();
                return;
            }
        }

        // This really only happens if you remove a block halfway thru casting ... but just to be safe.
        var blockChecking = this.level.getBlockState(this.nextBlock);
        if (!blockChecking.is(HexBlocks.SLATE.get())) {
            this.errorParticles(Vec3.atBottomCenterOf(this.nextBlock));
            this.stopCasting();
            return;
        }


    }

    protected void errorParticles(Vec3 pos) {
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

        this.level.setBlockAndUpdate(this.getBlockPos(),
            this.getBlockState().setValue(BlockAbstractImpetus.LIT, false));

        if (this.level.isClientSide) {
            Minecraft.getInstance().getSoundManager().stop(HexSounds.SPELL_CIRCLE_AMBIANCE.getId(), null);
        }
    }

    protected int getTickSpeed() {
        return 10;
    }


    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (this.activator != null && this.nextBlock != null && this.trackedBlocks != null) {
            tag.putUUID(TAG_ACTIVATOR, this.activator);
            tag.put(TAG_NEXT_BLOCK, NbtUtils.writeBlockPos(this.nextBlock));
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

        this.activator = tag.getUUID(TAG_ACTIVATOR);
        this.nextBlock = NbtUtils.readBlockPos(tag.getCompound(TAG_NEXT_BLOCK));
        var trackeds = tag.getList(TAG_TRACKED_BLOCKS, Tag.TAG_COMPOUND);
        this.trackedBlocks = new ArrayList<>(trackeds.size());
        for (var tracked : trackeds) {
            this.trackedBlocks.add(NbtUtils.readBlockPos((CompoundTag) tracked));
        }
    }
}
