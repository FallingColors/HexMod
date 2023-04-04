package at.petrak.hexcasting.common.blocks.decoration;

import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockSconce extends AmethystBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static VoxelShape AABB_UP = Block.box(4, 0, 4, 12, 1, 12);
    protected static VoxelShape AABB_DOWN = Block.box(4, 15, 4, 12, 16, 12);
    protected static VoxelShape AABB_NORTH = Block.box(4, 4, 15, 12, 12, 16);
    protected static VoxelShape AABB_SOUTH = Block.box(4, 4, 0, 12, 12, 1);
    protected static VoxelShape AABB_WEST = Block.box(15, 4, 4, 16, 12, 12);
    protected static VoxelShape AABB_EAST = Block.box(0, 4, 4, 1, 12, 12);

    public BlockSconce(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(WATERLOGGED, false).setValue(FACING, Direction.UP));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (pState.getValue(FACING)) {
            case UP -> AABB_UP;
            case DOWN -> AABB_DOWN;
            case NORTH -> AABB_NORTH;
            case EAST -> AABB_EAST;
            case SOUTH -> AABB_SOUTH;
            case WEST -> AABB_WEST;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidState = pContext.getLevel().getFluidState(pContext.getClickedPos());
        BlockState blockstate;
        blockstate = this.defaultBlockState().setValue(FACING, pContext.getClickedFace());
        blockstate = blockstate.setValue(WATERLOGGED,
                fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8);
        return blockstate;
    }
    
    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
        BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource rand) {
        if (rand.nextFloat() < 0.8f) {
            var cx = pPos.getX() + 0.5;
            var cy = pPos.getY() + 0.5;
            var cz = pPos.getZ() + 0.5;
            //values for particle direction randomization
            //x 
            var dX = switch(pState.getValue(FACING)){
                    case EAST -> rand.triangle(0.01f, 0.05f);
                    case WEST -> rand.triangle(-0.01f, -0.05f);
                    default -> rand.triangle(-0.01f, 0.01f);
            };
            //y
            var dY = switch(pState.getValue(FACING)){
                    case UP -> rand.triangle(0.01f, 0.05f);
                    case DOWN -> rand.triangle(-0.01f, -0.05f);
                    default -> rand.triangle(-0.01f, 0.01f);
            };
            //z
            var dZ = switch(pState.getValue(FACING)){
                    case SOUTH -> rand.triangle(0.01f, 0.05f);
                    case NORTH -> rand.triangle(-0.01f, -0.05f);
                    default -> rand.triangle(-0.01f, 0.01f);
            };
            int[] colors = {0xff_6f4fab, 0xff_b38ef3, 0xff_cfa0f3, 0xff_cfa0f3, 0xff_fffdd5};
            pLevel.addParticle(new ConjureParticleOptions(colors[rand.nextInt(colors.length)]), cx, cy, cz,
                dX, dY, dZ);
                
            if (rand.nextFloat() < 0.08f) {
                pLevel.playLocalSound(cx, cy, cz,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F,
                    0.5F + rand.nextFloat() * 1.2F, false);
            }
        }
    }
}
