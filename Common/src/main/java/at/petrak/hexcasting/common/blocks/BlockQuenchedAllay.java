package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.common.blocks.entity.BlockEntityQuenchedAllay;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BlockQuenchedAllay extends Block implements EntityBlock {
    public static final int VARIANTS = 4;

    public BlockQuenchedAllay(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityQuenchedAllay(this, pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        ParticleOptions options = new ConjureParticleOptions(0x8932b8);
        Vec3 center = Vec3.atCenterOf(pos);
        for (Direction direction : Direction.values()) {
            int dX = direction.getStepX();
            int dY = direction.getStepY();
            int dZ = direction.getStepZ();

            int count = rand.nextInt(10) / 4;
            for (int i = 0; i < count; i++) {
                double pX = center.x + (dX == 0 ? Mth.nextDouble(rand, -0.5D, 0.5D) : (double) dX * 0.55D);
                double pY = center.y + (dY == 0 ? Mth.nextDouble(rand, -0.5D, 0.5D) : (double) dY * 0.55D);
                double pZ = center.z + (dZ == 0 ? Mth.nextDouble(rand, -0.5D, 0.5D) : (double) dZ * 0.55D);
                double vPerp = Mth.nextDouble(rand, 0.0, 0.01);
                double vX = vPerp * dX;
                double vY = vPerp * dY;
                double vZ = vPerp * dZ;
                level.addParticle(options, pX, pY, pZ, vX, vY, vZ);
            }
        }
    }
}
