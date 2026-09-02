package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockNeuralMesh extends MultifaceBlock {
    public static final MapCodec<BlockNeuralMesh> CODEC = simpleCodec(BlockNeuralMesh::new);
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public MapCodec<BlockNeuralMesh> codec() {
        return CODEC;
    }

    public BlockNeuralMesh(BlockBehaviour.Properties arg) {
        super(arg);
    }

    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return blockPlaceContext.getItemInHand().is(HexItems.NEURAL_MESH.get());
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        for (var dir : availableFaces(blockState)) {
            BlockPos targetPos = blockPos.relative(dir);
            BlockState targetState = serverLevel.getBlockState(targetPos);
            if (targetState.is(Blocks.BUDDING_AMETHYST) && randomSource.nextDouble() < 0.5) {
                targetState.randomTick(serverLevel, targetPos, randomSource);
            }
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!hasAnyFace(blockState)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            if (hasFace(blockState, direction) && !canAttachTo(levelAccessor, direction, blockPos2, blockState2)) {
                var resultState = removeFace(blockState, getFaceProperty(direction));
                if (resultState.is(HexBlocks.NEURAL_MESH.get())) {
                    if (!levelAccessor.isClientSide() && levelAccessor instanceof Level level) {
                        var pos = blockPos.getCenter();
                        var entity = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(HexItems.NEURAL_MESH.get()));
                        entity.setDefaultPickUpDelay();
                        level.addFreshEntity(entity);
                    }
                }
                return resultState;
            }
            return blockState;
        }
    }

    // this is private in MultifaceBlock so here's a reimpl
    private static BlockState removeFace(BlockState blockState, BooleanProperty booleanProperty) {
        BlockState blockState2 = (BlockState)blockState.setValue(booleanProperty, false);
        return hasAnyFace(blockState2) ? blockState2 : Blocks.AIR.defaultBlockState();
    }
}
