package at.petrak.hexcasting.common.blocks.circles;

import at.petrak.hexcasting.api.BlockCircleComponent;
import at.petrak.hexcasting.hexmath.HexPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

// FACING is the direction the *bottom* of the pattern points
// (or which way is "down")
public class BlockSlate extends BlockCircleComponent implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected static final double THICKNESS = 1;
    protected static final VoxelShape AABB_FLOOR = Block.box(0, 0, 0, 16, THICKNESS, 16);

    public BlockSlate(Properties p_53182_) {
        super(p_53182_);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(ENERGIZED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, Level world) {
        return enterDir != Direction.UP;
    }

    @Override
    public EnumSet<Direction> exitDirections(BlockPos pos, BlockState bs, Level world) {
        return EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    }

    @Override
    public @Nullable HexPattern getPattern(BlockPos pos, BlockState bs, Level world) {
        if (world.getBlockEntity(pos) instanceof BlockEntitySlate tile) {
            return tile.pattern;
        } else {
            return null;
        }
    }

    @Override
    public Direction particleOutDir(BlockPos pos, BlockState bs, Level world) {
        return Direction.UP;
    }

    @Override
    public float particleHeight(BlockPos pos, BlockState bs, Level world) {
        return 0.5f - 15f / 16f;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntitySlate(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AABB_FLOOR;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.DESTROY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    // i do as the TorchBlock.java guides
    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return canSupportCenter(pLevel, pPos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
        BlockPos pCurrentPos, BlockPos pFacingPos) {
        return pFacing == Direction.DOWN && !this.canSurvive(pState, pLevel,
            pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel,
            pCurrentPos, pFacingPos);
    }


}
