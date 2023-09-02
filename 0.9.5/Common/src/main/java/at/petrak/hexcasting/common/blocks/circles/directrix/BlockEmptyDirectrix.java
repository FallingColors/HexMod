package at.petrak.hexcasting.common.blocks.circles.directrix;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class BlockEmptyDirectrix extends BlockCircleComponent {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public BlockEmptyDirectrix(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(ENERGIZED, false)
            .setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, Direction normalDir, BlockPos pos, BlockState bs,
        Level world) {
        return true;
    }

    @Override
    public EnumSet<Direction> exitDirections(BlockPos pos, BlockState bs, Level world) {
        var sign = world.random.nextBoolean() ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        return EnumSet.of(Direction.fromAxisAndDirection(bs.getValue(AXIS), sign));
    }

    @Override
    public @Nullable HexPattern getPattern(BlockPos pos, BlockState bs, Level world) {
        return null;
    }

    @Override
    public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft) {
        return Direction.UP;
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
        builder.add(AXIS);
    }


    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(AXIS, pContext.getNearestLookingDirection().getAxis());
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(AXIS,
            pRot.rotate(Direction.get(Direction.AxisDirection.POSITIVE, pState.getValue(AXIS))).getAxis());
    }
}
