package at.petrak.hexcasting.common.blocks.decoration;

import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
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
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, Random rand) {
        if (rand.nextFloat() < 0.8f) {
            var cx = pPos.getX() + 0.5;
            var cy = pPos.getY() + 0.5;
            var cz = pPos.getZ() + 0.5;
            int[] colors = {0xff_6f4fab, 0xff_b38ef3, 0xff_cfa0f3, 0xff_cfa0f3, 0xff_fffdd5};
            pLevel.addParticle(new ConjureParticleOptions(colors[rand.nextInt(colors.length)], true), cx, cy, cz,
                rand.nextFloat(-0.01f, 0.01f), rand.nextFloat(0.01f, 0.05f), rand.nextFloat(-0.01f, 0.01f));
            if (rand.nextFloat() < 0.08f) {
                pLevel.playLocalSound(cx, cy, cz,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F,
                    0.5F + rand.nextFloat() * 1.2F, false);
            }
        }
    }
}
