package at.petrak.hexcasting.common.blocks.circles.directrix;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.BooleanIota;
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapBoolDirectrixEmptyStack;
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapBoolDirectrixNotBool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

// When pops a true, outputs to FACING, when pops a false, outputs to the opposite
public class BlockBooleanDirectrix extends BlockCircleComponent {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);

    public BlockBooleanDirectrix(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(ENERGIZED, false)
            .setValue(STATE, State.NEITHER)
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos,
        BlockState bs, ServerLevel world) {
        var stack = new ArrayList<>(imageIn.getStack());
        if (stack.isEmpty()) {
            this.fakeThrowMishap(pos, bs, imageIn, env,
                new MishapBoolDirectrixEmptyStack(pos));
            return new ControlFlow.Stop();
        }
        var last = stack.remove(stack.size() - 1);

        if (!(last instanceof BooleanIota biota)) {
            this.fakeThrowMishap(pos, bs, imageIn, env,
                new MishapBoolDirectrixNotBool(last, pos));
            return new ControlFlow.Stop();
        }

        world.setBlockAndUpdate(pos, bs.setValue(STATE, biota.getBool() ? State.TRUE : State.FALSE));

        var outputDir = biota.getBool()
            ? bs.getValue(FACING).getOpposite()
            : bs.getValue(FACING);
        var imageOut = imageIn.copy(stack, imageIn.getParenCount(), imageIn.getParenthesized(),
            imageIn.getEscapeNext(), imageIn.getOpsConsumed(), imageIn.getUserData());

        return new ControlFlow.Continue(imageOut, List.of(this.exitPositionFromDirection(pos, outputDir)));
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, ServerLevel world) {
        // No entering from the front, no entering from the back.
        return enterDir != bs.getValue(FACING).getOpposite() && enterDir != bs.getValue(FACING);
    }

    @Override
    public EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, Level world) {
        return EnumSet.of(bs.getValue(FACING), bs.getValue(FACING).getOpposite());
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
    public BlockState endEnergized(BlockPos pos, BlockState bs, Level world) {
        var newState = bs.setValue(ENERGIZED, false).setValue(STATE, State.NEITHER);
        world.setBlockAndUpdate(pos, newState);
        return newState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE, FACING);
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

    public enum State implements StringRepresentable {
        // Freshly started
        NEITHER,
        // Popped a true
        TRUE,
        // Popped a false
        FALSE,
        ;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
