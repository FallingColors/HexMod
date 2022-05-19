package at.petrak.hexcasting.common.blocks.circles.directrix;

import at.petrak.hexcasting.api.block.circle.BlockCircleComponent;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Random;

public class BlockRedstoneDirectrix extends BlockCircleComponent {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty REDSTONE_POWERED = BlockStateProperties.POWERED;

    public BlockRedstoneDirectrix(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(REDSTONE_POWERED, false)
            .setValue(ENERGIZED, false)
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean canEnterFromDirection(Direction enterDir, Direction normalDir, BlockPos pos, BlockState bs,
        Level world) {
        return enterDir != getRealFacing(bs);
    }

    @Override
    public EnumSet<Direction> exitDirections(BlockPos pos, BlockState bs, Level world) {
        return EnumSet.of(getRealFacing(bs));
    }

    @Override
    public @Nullable HexPattern getPattern(BlockPos pos, BlockState bs, Level world) {
        return null;
    }

    @Override
    public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft) {
        return normalDirOfOther(pos.relative(getRealFacing(bs)), world, recursionLeft);
    }

    @Override
    public float particleHeight(BlockPos pos, BlockState bs, Level world) {
        return 0.5f;
    }

    protected Direction getRealFacing(BlockState bs) {
        var facing = bs.getValue(FACING);
        if (bs.getValue(REDSTONE_POWERED)) {
            return facing.getOpposite();
        } else {
            return facing;
        }
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
        boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);

        if (!pLevel.isClientSide) {
            boolean currentlyPowered = pState.getValue(REDSTONE_POWERED);
            if (currentlyPowered != pLevel.hasNeighborSignal(pPos)) {
                pLevel.setBlock(pPos, pState.setValue(REDSTONE_POWERED, !currentlyPowered), 2);
            }
        }
    }


    @Override
    public void animateTick(BlockState bs, Level pLevel, BlockPos pos, Random rand) {
        if (bs.getValue(REDSTONE_POWERED)) {
            for (int i = 0; i < 2; i++) {
                var step = bs.getValue(FACING).getOpposite().step();
                var center = Vec3.atCenterOf(pos).add(step.x() * 0.5, step.y() * 0.5, step.z() * 0.5);
                double x = center.x + (rand.nextDouble() - 0.5) * 0.5D;
                double y = center.y + (rand.nextDouble() - 0.5) * 0.5D;
                double z = center.z + (rand.nextDouble() - 0.5) * 0.5D;
                pLevel.addParticle(DustParticleOptions.REDSTONE, x, y, z,
                    step.x() * 0.1, step.y() * 0.1, step.z() * 0.1);
            }
        }
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.BLOCK;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(REDSTONE_POWERED, FACING);
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
