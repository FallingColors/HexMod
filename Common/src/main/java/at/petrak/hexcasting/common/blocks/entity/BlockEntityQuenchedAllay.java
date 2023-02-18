package at.petrak.hexcasting.common.blocks.entity;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * No-op BE just to have a BER
 */
public class BlockEntityQuenchedAllay extends HexBlockEntity {
    public BlockEntityQuenchedAllay(BlockPos pos, BlockState blockState) {
        super(HexBlockEntities.QUENCHED_ALLAY_TILE, pos, blockState);
    }

    @Override
    protected void saveModData(CompoundTag tag) {

    }

    @Override
    protected void loadModData(CompoundTag tag) {

    }
}
