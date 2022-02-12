package at.petrak.hexcasting.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockSlate extends FaceAttachedHorizontalDirectionalBlock {

    protected static final double THICKNESS = 1;
    protected static final VoxelShape AABB_FLOOR = Block.box(0, 0, 0, 16, THICKNESS, 16);
    protected static final VoxelShape AABB_CEILING = Block.box(0, 16 - THICKNESS, 0, 16, 16, 16);
    protected static final VoxelShape AABB_EAST_WALL = Block.box(0, 0, 0, THICKNESS, 16, 16);
    protected static final VoxelShape AABB_WEST_WALL = Block.box(0, 16 - THICKNESS, 0, 16, 16, 16);
    protected static final VoxelShape AABB_SOUTH_WALL = Block.box(0, 0, 0, 16, 16, THICKNESS);
    protected static final VoxelShape AABB_NORTH_WALL = Block.box(0, 0, 16 - THICKNESS, 16, 16, 16);

    public BlockSlate(Properties p_53182_) {
        super(p_53182_);
        this.registerDefaultState(
            this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(FACE)) {
            case FLOOR -> AABB_FLOOR;
            case CEILING -> AABB_CEILING;
            case WALL -> switch (pState.getValue(FACING)) {
                case EAST -> AABB_EAST_WALL;
                case WEST -> AABB_WEST_WALL;
                case SOUTH -> AABB_SOUTH_WALL;
                // North case. I need to cover up and down, but presumably those
                // states are illegal?
                default -> AABB_NORTH_WALL;
            };
        };
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.DESTROY;
    }
}
