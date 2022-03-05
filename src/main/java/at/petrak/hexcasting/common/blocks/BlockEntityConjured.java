package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.common.casting.colors.FrozenColorizer;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class BlockEntityConjured extends BlockEntity {
    private static final Random RANDOM = new Random();
    private FrozenColorizer colorizer = FrozenColorizer.DEFAULT;

    public static final String TAG_COLORIZER = "tag_colorizer";

    public BlockEntityConjured(BlockPos pos, BlockState state) {
        super(HexBlocks.CONJURED_TILE.get(), pos, state);
    }

    public void walkParticle(Entity pEntity) {
        if (getBlockState().getBlock() instanceof BlockConjured) {
            for (int i = 0; i < 3; ++i) {
                int color = this.colorizer.getColor(pEntity.tickCount, pEntity.position()
                    .add(new Vec3(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()).scale(
                        RANDOM.nextFloat() * 3)));
                assert level != null;
                level.addParticle(new ConjureParticleOptions(color, false),
                    pEntity.getX() + (RANDOM.nextFloat() * 0.6D) - 0.3D,
                    getBlockPos().getY() + (RANDOM.nextFloat() * 0.05D) + 0.95D,
                    pEntity.getZ() + (RANDOM.nextFloat() * 0.6D) - 0.3D,
                    0.0, 0.0, 0.0);
            }
        }
    }

    public void particleEffect() {
        if (getBlockState().getBlock() instanceof BlockConjured) {
            int color = this.colorizer.getColor(RANDOM.nextFloat() * 16384,
                new Vec3(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()).scale(
                    RANDOM.nextFloat() * 3));
            assert level != null;
            if (getBlockState().getValue(BlockConjured.LIGHT)) {
                level.addParticle(new ConjureParticleOptions(color, true),
                    (double) getBlockPos().getX() + 0.4D + (RANDOM.nextFloat() * 0.2D),
                    (double) getBlockPos().getY() + 0.4D + (RANDOM.nextFloat() * 0.2D),
                    (double) getBlockPos().getZ() + 0.4D + (RANDOM.nextFloat() * 0.2D),
                    0.0, 0.0, 0.0);
            } else {
                if (RANDOM.nextFloat() < 0.7) {
                    level.addParticle(new ConjureParticleOptions(color, false),
                        (double) getBlockPos().getX() + RANDOM.nextFloat(),
                        (double) getBlockPos().getY() + RANDOM.nextFloat(),
                        (double) getBlockPos().getZ() + RANDOM.nextFloat(),
                        0.0, 0.0, 0.0);
                }
            }
        }
    }

    public void landParticle(Entity entity, int number) {
        if (getBlockState().getBlock() instanceof BlockConjured) {
            for (int i = 0; i < number * 2; i++) {
                int color = this.colorizer.getColor(entity.tickCount, entity.position()
                    .add(new Vec3(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat()).scale(
                        RANDOM.nextFloat() * 3)));
                assert level != null;
                level.addParticle(new ConjureParticleOptions(color, false),
                    entity.getX() + (RANDOM.nextFloat() * 0.8D) - 0.2D,
                    getBlockPos().getY() + (RANDOM.nextFloat() * 0.05D) + 0.95D,
                    entity.getZ() + (RANDOM.nextFloat() * 0.8D) - 0.2D,
                    0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag pTag) {
        pTag.put(TAG_COLORIZER, this.colorizer.serialize());
        super.saveAdditional(pTag);
    }

    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        readPacketNBT(tag);
    }

    public FrozenColorizer getColorizer() {
        return this.colorizer;
    }

    public void setColorizer(FrozenColorizer colorizer) {
        this.colorizer = colorizer;
        this.setChanged();
    }

    @Override
    public void setChanged() {
        if (this.level == null) {
            return;
        }
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
            Block.UPDATE_CLIENTS);
        super.setChanged();
    }

    public void readPacketNBT(CompoundTag tag) {
        if (tag.contains(TAG_COLORIZER)) {
            this.setColorizer(FrozenColorizer.deserialize(tag.getCompound(TAG_COLORIZER)));
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(Objects.requireNonNull(pkt.getTag()));
    }

    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}
