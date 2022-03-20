package at.petrak.hexcasting.api;

import at.petrak.hexcasting.hexmath.HexPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public abstract class BlockCircleComponent extends Block {
    public static final BooleanProperty ENERGIZED = BooleanProperty.create("energized");

    public BlockCircleComponent(Properties p_49795_) {
        super(p_49795_);
    }

    abstract public Direction[] exitDirections(BlockPos pos, BlockState bs, ServerLevel world);

    @Nullable
    abstract public HexPattern getPattern(BlockPos pos, BlockState bs, ServerLevel world);

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
