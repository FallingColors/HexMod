package at.petrak.hexcasting.common.blocks.circles;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.common.blocks.circles.BlockSlate.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockChestLocus extends BlockSlate {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    private static final VoxelShape BOX_FLOOR = Shapes.or(AABB_FLOOR, Block.box(1, 1, 1, 15, 9, 15));
    private static final VoxelShape BOX_CEILING = Shapes.or(AABB_CEILING, Block.box(1, 7, 1, 15, 15, 15));
    private static final VoxelShape BOX_EAST = Shapes.or(AABB_EAST_WALL, Block.box(1, 1, 1, 9, 15, 15));
    private static final VoxelShape BOX_WEST = Shapes.or(AABB_WEST_WALL, Block.box(7, 1, 1, 15, 15, 15));
    private static final VoxelShape BOX_SOUTH = Shapes.or(AABB_SOUTH_WALL, Block.box(1, 1, 1, 15, 15, 9));
    private static final VoxelShape BOX_NORTH = Shapes.or(AABB_NORTH_WALL, Block.box(1, 1, 7, 15, 15, 15));

    public BlockChestLocus(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(OPEN, false)
                .setValue(ENERGIZED, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityChestLocus(blockPos, blockState);
    }

    @Override
    public ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos,
        BlockState bs, ServerLevel world) {

        env.circleState().inventoryPos = pos;

        var exitDirsSet = this.possibleExitDirections(pos, bs, world);
        exitDirsSet.remove(enterDir.getOpposite());
        var exitDirs = exitDirsSet.stream().map((dir) -> this.exitPositionFromDirection(pos, dir));

        return new ControlFlow.Continue(imageIn, exitDirs.toList());
    }

    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BlockEntityChestLocus) {
            player.openMenu((BlockEntityChestLocus)blockEntity);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        Containers.dropContentsOnDestroy(blockState, blockState2, level, blockPos);
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(ATTACH_FACE)) {
            case FLOOR -> BOX_FLOOR;
            case CEILING -> BOX_CEILING;
            case WALL -> switch (pState.getValue(FACING)) {
                case NORTH -> BOX_NORTH;
                case EAST -> BOX_EAST;
                case SOUTH -> BOX_SOUTH;
                default -> BOX_WEST;
            };
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN);
    }
}
