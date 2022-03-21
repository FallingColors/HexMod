package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.BlockCircleComponent;
import at.petrak.hexcasting.hexmath.HexPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Random;

// Facing dir is the direction it starts searching for slates in to start
public abstract class BlockAbstractImpetus extends BlockCircleComponent implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlockAbstractImpetus(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(ENERGIZED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, Level world) {
        return true;
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
    public Direction particleOutDir(BlockPos pos, BlockState bs, Level world) {
        BlockPos neighborPos = pos.relative(bs.getValue(FACING));
        var neighbor = world.getBlockState(neighborPos);
        if (neighbor.getBlock() instanceof BlockCircleComponent bcc && !(bcc instanceof BlockAbstractImpetus)) {
            return bcc.particleOutDir(neighborPos, neighbor, world);
        } else {
            // Who knows
            return Direction.UP;
        }
    }

    @Override
    public float particleHeight(BlockPos pos, BlockState bs, Level world) {
        return 0.5f;
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        if (pLevel.getBlockEntity(pPos) instanceof BlockEntityAbstractImpetus tile) {
            tile.stepCircle();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
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
