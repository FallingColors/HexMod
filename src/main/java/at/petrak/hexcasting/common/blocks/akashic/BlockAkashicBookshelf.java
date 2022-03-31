package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.common.blocks.HexBlockTags;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockAkashicBookshelf extends Block {
    public BlockAkashicBookshelf(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public void onPlace(BlockState pState, Level world, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        if (world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile) {
            var recordPos = BlockEntityAkashicRecord.floodFillFor(pos, world,
                (here, bs) -> bs.is(HexBlockTags.AKASHIC_FLOODFILLER),
                (here, bs) -> bs.is(HexBlocks.AKASHIC_RECORD.get()));
            if (recordPos != null) {
                tile.recordPos = recordPos;
                tile.setChanged();
            }
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pLevel.getBlockEntity(pPos) instanceof BlockEntityAkashicBookshelf tile &&
            tile.recordPos != null && pLevel.getBlockEntity(tile.recordPos) instanceof BlockEntityAkashicRecord rec) {
            rec.removeFloodfillerAt(pPos);
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
