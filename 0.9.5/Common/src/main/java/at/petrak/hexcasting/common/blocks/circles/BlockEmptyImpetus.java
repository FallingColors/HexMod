package at.petrak.hexcasting.common.blocks.circles;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

// As it turns out, not actually an impetus
public class BlockEmptyImpetus extends BlockCircleComponent {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockEmptyImpetus(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(ENERGIZED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, Direction normalDir, BlockPos pos, BlockState bs,
        Level world) {
        return enterDir != bs.getValue(FACING);
    }

    @Override
    public EnumSet<Direction> exitDirections(BlockPos pos, BlockState bs, Level world) {
        return EnumSet.of(bs.getValue(FACING));
    }

    @Override
    public @Nullable HexPattern getPattern(BlockPos pos, BlockState bs, Level world) {
        return null;
    }

    @Override
    public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft) {
        return normalDirOfOther(pos.relative(bs.getValue(FACING)), world, recursionLeft);
    }

    @Override
    public float particleHeight(BlockPos pos, BlockState bs, Level world) {
        return 0.5f;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.BLOCK;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection());
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }
}
