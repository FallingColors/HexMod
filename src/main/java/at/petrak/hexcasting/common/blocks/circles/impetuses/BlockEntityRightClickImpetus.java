package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.circle.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityRightClickImpetus extends BlockEntityAbstractImpetus {
    public BlockEntityRightClickImpetus(BlockPos pWorldPosition, BlockState pBlockState) {
        super(HexBlocks.IMPETUS_RIGHTCLICK_TILE.get(), pWorldPosition, pBlockState);
    }

    @Override
    public boolean activatorAlwaysInRange() {
        return false;
    }
}
