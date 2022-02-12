package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.common.casting.colors.FrozenColorizer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockConjured extends Block implements SimpleWaterloggedBlock, EntityBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIGHT = BooleanProperty.create("light");
    private static final VoxelShape LIGHT_SHAPE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);

    public BlockConjured(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT, false).setValue(WATERLOGGED, false));
    }

    @Override
    public void stepOn(Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, @NotNull Entity pEntity) {
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (tile instanceof BlockEntityConjured bec) {
            bec.walkParticle(pEntity);
        }
        super.stepOn(pLevel, pPos, pState, pEntity);
    }

    @Override
    public void animateTick(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos,
        @NotNull Random pRand) {
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (tile instanceof BlockEntityConjured bec) {
            bec.particleEffect();
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new BlockEntityConjured(pPos, pState);
    }

    public static void setColor(LevelAccessor pLevel, BlockPos pPos, FrozenColorizer colorizer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof BlockEntityConjured tile) {
            tile.setColorizer(colorizer);
        }
    }

    @Override
    public void onPlace(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pOldState,
        boolean pIsMoving) {
        pLevel.sendBlockUpdated(pPos, pState, pState, Block.UPDATE_CLIENTS);
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos,
        @NotNull CollisionContext context) {
        return state.getValue(LIGHT) ? LIGHT_SHAPE : super.getShape(state, level, pos, context);
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return state.getValue(LIGHT) ? PushReaction.DESTROY : super.getPistonPushReaction(state);
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState pState, @NotNull BlockGetter pLevel,
        @NotNull BlockPos pPos) {
        return true;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(LIGHT) ? 15 : 2;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos,
        @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type,
        EntityType<?> entityType) {
        return false;
    }

    @Override
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel,
        @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0F;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter pLevel,
        @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return state.getValue(LIGHT) ? Shapes.empty() : super.getCollisionShape(state, pLevel, pPos, pContext);
    }

    @Override
    protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState) {
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2,
        LivingEntity entity, int numberOfParticles) {
        BlockEntity tile = worldserver.getBlockEntity(pos);
        if (tile instanceof BlockEntityConjured) {
            ((BlockEntityConjured) tile).landParticle(entity, numberOfParticles);
        }
        return true;
    }
}

