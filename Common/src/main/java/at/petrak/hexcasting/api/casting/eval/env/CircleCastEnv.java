package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.api.misc.FrozenColorizer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CircleCastEnv extends CastingEnvironment {
    protected final BlockPos impetusLoc;
    protected final Direction startDir;

    public CircleCastEnv(ServerLevel world, BlockPos impetusLoc, Direction startDir) {
        super(world);
        this.impetusLoc = impetusLoc;
        this.startDir = startDir;
    }

    @Override
    public @Nullable ServerPlayer getCaster() {
        return null;
    }

    public @Nullable BlockEntityAbstractImpetus getCircle() {
        var entity = this.world.getBlockEntity(impetusLoc);

        if (entity instanceof BlockEntityAbstractImpetus)
            return (BlockEntityAbstractImpetus) entity;
        return null;
    }

    public BlockPos getImpetusLoc() {
        return impetusLoc;
    }

    public Direction getStartDir() {
        return startDir;
    }

    @Override
    public MishapEnvironment getMishapEnvironment() {
        return null;
    }

    @Override
    public EvalSound getSoundType() {
        return null;
    }

    @Override
    public void postExecution(CastResult result) {

    }

    @Override
    public Vec3 mishapSprayPos() {
        return null;
    }

    @Override
    public long extractMedia(long cost) {
        return 0;
    }

    @Override
    public boolean isVecInRange(Vec3 vec) {
        return false;
    }

    @Override
    public InteractionHand castingHand() {
        return null;
    }

    @Override
    public ItemStack getAlternateItem() {
        return null;
    }

    @Override
    protected List<ItemStack> getUsableStacks(StackDiscoveryMode mode) {
        return null;
    }

    @Override
    public FrozenColorizer getColorizer() {
        return null;
    }

    @Override
    public void produceParticles(ParticleSpray particles, FrozenColorizer colorizer) {

    }
}
