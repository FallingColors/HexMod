package at.petrak.hexcasting.common.blocks.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

public class BlockSconce extends AmethystBlock {
    protected static VoxelShape AABB = Block.box(4, 0, 4, 12, 1, 12);

    public BlockSconce(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return AABB;
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random pRandom) {
        if (pRandom.nextFloat() < 0.8f) {
            pLevel.playSound(null, pPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F,
                0.5F + pRandom.nextFloat() * 1.2F);
        }
    }
}
