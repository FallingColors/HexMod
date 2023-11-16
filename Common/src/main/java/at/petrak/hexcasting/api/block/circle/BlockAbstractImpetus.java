package at.petrak.hexcasting.api.block.circle;

import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.EnumSet;

// Facing dir is the direction it starts searching for slates in to start
public abstract class BlockAbstractImpetus extends BlockCircleComponent implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockAbstractImpetus(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(ENERGIZED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos,
        BlockState bs, ServerLevel world) {
        return new ControlFlow.Stop();
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, ServerLevel world) {
        // FACING is the direction media EXITS from, so we can't have media entering in that direction
        // so, flip it
        return enterDir != bs.getValue(FACING).getOpposite();
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, Level world) {
        return EnumSet.of(bs.getValue(FACING));
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
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pLevel.getBlockEntity(pPos) instanceof BlockEntityAbstractImpetus tile && pState.getValue(ENERGIZED)) {
            tile.tickExecution();
        }
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pNewState.is(pState.getBlock())
            && pLevel.getBlockEntity(pPos) instanceof BlockEntityAbstractImpetus impetus) {
            impetus.endExecution(); // TODO: Determine if this was important
            // TODO: Fix this, it should make all the glowy circle components stop glowing.
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return BlockCircleComponent.placeStateDirAndSneak(this.defaultBlockState(), pContext);
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
