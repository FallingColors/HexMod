package at.petrak.hexcasting.api.block.circle;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.EnumSet;

public abstract class BlockCircleComponent extends Block implements ICircleComponent {
    public static final BooleanProperty ENERGIZED = BooleanProperty.create("energized");

    public BlockCircleComponent(Properties p_49795_) {
        super(p_49795_);
    }

    /**
     * Which direction points "up" or "out" for this block?
     * This is used for {@link ICircleComponent#canEnterFromDirection(Direction, BlockPos, BlockState, ServerLevel)}
     * as well as particles.
     */
    public Direction normalDir(BlockPos pos, BlockState bs, Level world) {
        return normalDir(pos, bs, world, 16);
    }

    abstract public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft);

    public static Direction normalDirOfOther(BlockPos other, Level world, int recursionLeft) {
        if (recursionLeft <= 0) {
            return Direction.UP;
        }

        var stateThere = world.getBlockState(other);
        if (stateThere.getBlock() instanceof BlockCircleComponent bcc) {
            return bcc.normalDir(other, stateThere, world, recursionLeft - 1);
        } else {
            return Direction.UP;
        }
    }

    /**
     * How many blocks in the {@link BlockCircleComponent#normalDir(BlockPos, BlockState, Level)} from the center
     * particles should be spawned in
     */
    abstract public float particleHeight(BlockPos pos, BlockState bs, Level world);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ENERGIZED);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return pState.getValue(ENERGIZED) ? 15 : 0;
    }
}
