package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.common.items.colorizer.ItemColorizer;
import at.petrak.hexcasting.common.particles.HexParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;

public class BlockEntityConjured extends BlockEntity {
    private static final Random RANDOM = new Random();
    private ItemStack color = ItemStack.EMPTY;

    public BlockEntityConjured(BlockPos pos, BlockState state) {
        super(HexBlocks.CONJURED_TILE.get(), pos, state);
    }

    public void walkParticle(Entity pEntity) {
        int[] colors;
        if (color.getItem() instanceof ItemColorizer colorizer) {
            colors = colorizer.getColors();
        } else {
            colors = new int[]{11767539};
        }

        if (getBlockState().getBlock() instanceof BlockConjured) {
            for (int i = 0; i < 3; ++i) {
                int j = RANDOM.nextInt(colors.length);
                double d0 = (double)(colors[j] >> 16 & 255) / 255.0D;
                double d1 = (double)(colors[j] >> 8 & 255) / 255.0D;
                double d2 = (double)(colors[j] & 255) / 255.0D;
                assert level != null;
                level.addParticle(HexParticles.CONJURE_BLOCK_PARTICLE.get(),
                        pEntity.getX() + (RANDOM.nextFloat() * 0.6D) - 0.3D,
                        getBlockPos().getY() + (RANDOM.nextFloat() * 0.05D) + 0.95D,
                        pEntity.getZ() + (RANDOM.nextFloat() * 0.6D) - 0.3D, d0, d1, d2);
            }
        }
    }

    public void particleEffect() {
        int[] colors;
        if (color.getItem() instanceof ItemColorizer colorizer) {
            colors = colorizer.getColors();
        } else {
            colors = new int[]{11767539};
        }

        if (getBlockState().getBlock() instanceof BlockConjured) {
            if (getBlockState().getValue(BlockConjured.LIGHT)) {
                int j = RANDOM.nextInt(colors.length);
                double d0 = (double)(colors[j] >> 16 & 255) / 255.0D;
                double d1 = (double)(colors[j] >> 8 & 255) / 255.0D;
                double d2 = (double)(colors[j] & 255) / 255.0D;
                assert level != null;
                level.addParticle(HexParticles.CONJURE_LIGHT_PARTICLE.get(),
                        (double)getBlockPos().getX() + 0.4D + (RANDOM.nextFloat() * 0.2D),
                        (double)getBlockPos().getY() + 0.4D + (RANDOM.nextFloat() * 0.2D),
                        (double)getBlockPos().getZ() + 0.4D + (RANDOM.nextFloat() * 0.2D), d0, d1, d2);
            } else {
                if (RANDOM.nextBoolean()) {
                    int j = RANDOM.nextInt(colors.length);
                    double d0 = (double) (colors[j] >> 16 & 255) / 255.0D;
                    double d1 = (double) (colors[j] >> 8 & 255) / 255.0D;
                    double d2 = (double) (colors[j] & 255) / 255.0D;
                    assert level != null;
                    level.addParticle(HexParticles.CONJURE_BLOCK_PARTICLE.get(),
                            (double) getBlockPos().getX() + RANDOM.nextFloat(),
                            (double) getBlockPos().getY() + RANDOM.nextFloat(),
                            (double) getBlockPos().getZ() + RANDOM.nextFloat(), d0, d1, d2);
                }
            }
        }
    }

    public void landParticle(Entity entity, int number) {
        int[] colors;
        if (color.getItem() instanceof ItemColorizer colorizer) {
            colors = colorizer.getColors();
        } else {
            colors = new int[]{11767539};
        }

        if (getBlockState().getBlock() instanceof BlockConjured) {
            for (int i = 0; i < number * 2; i++) {
                int j = RANDOM.nextInt(colors.length);
                double d0 = (double)(colors[j] >> 16 & 255) / 255.0D;
                double d1 = (double)(colors[j] >> 8 & 255) / 255.0D;
                double d2 = (double)(colors[j] & 255) / 255.0D;
                assert level != null;
                level.addParticle(HexParticles.CONJURE_BLOCK_PARTICLE.get(),
                        entity.getX() + (RANDOM.nextFloat() * 0.8D) - 0.2D,
                        getBlockPos().getY() + (RANDOM.nextFloat() * 0.05D) + 0.95D,
                        entity.getZ() + (RANDOM.nextFloat() * 0.8D) - 0.2D, d0, d1, d2);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag pTag) {
        pTag.put("Colorizer", this.getColor().save(new CompoundTag()));
        super.saveAdditional(pTag);
    }

    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        readPacketNBT(tag);
    }

    public ItemStack getColor() {
        return this.color;
    }

    public void setColor(ItemStack colourizer) {
        this.color = colourizer;
        this.setChanged();
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        super.setChanged();
    }

    public void readPacketNBT(CompoundTag tag) {
        if (tag.contains("Colorizer")) {
            this.setColor(ItemStack.of(tag.getCompound("Colorizer")));
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
