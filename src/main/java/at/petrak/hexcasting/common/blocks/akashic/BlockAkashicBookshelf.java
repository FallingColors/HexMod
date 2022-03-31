package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.common.blocks.HexBlockTags;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public class BlockAkashicBookshelf extends HorizontalDirectionalBlock implements EntityBlock {
    public BlockAkashicBookshelf(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityAkashicBookshelf(pPos, pState);
    }
}
