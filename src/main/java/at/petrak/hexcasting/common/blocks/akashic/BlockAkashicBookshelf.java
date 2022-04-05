package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class BlockAkashicBookshelf extends BlockAkashicFloodfiller implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DatumType> DATUM_TYPE = EnumProperty.create("datum_type", DatumType.class);

    public BlockAkashicBookshelf(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.getStateDefinition().any()
            .setValue(FACING, Direction.NORTH)
            .setValue(DATUM_TYPE, DatumType.EMPTY));
    }

    // aa

    @Override
    public @Nullable BlockPos getRecordPosition(BlockPos herePos, BlockState state, Level world) {
        if (world.getBlockEntity(herePos) instanceof BlockEntityAkashicBookshelf tile &&
            tile.recordPos != null && world.getBlockEntity(tile.recordPos) instanceof BlockEntityAkashicRecord) {
            return tile.recordPos;
        }
        return super.getRecordPosition(herePos, state, world);
    }

    @Override
    public void onPlace(BlockState pState, Level world, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        if (world.getBlockEntity(pos) instanceof BlockEntityAkashicBookshelf tile) {
            var recordPos = BlockAkashicFloodfiller.floodFillFor(pos, world,
                (here, bs, level) -> bs.is(HexBlocks.AKASHIC_RECORD.get()));
            if (recordPos != null) {
                tile.recordPos = recordPos;
                tile.setChanged();
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DATUM_TYPE);
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

    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }
}
