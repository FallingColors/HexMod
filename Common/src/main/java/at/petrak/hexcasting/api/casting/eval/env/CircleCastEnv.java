package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.api.misc.FrozenColorizer;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CircleCastEnv extends CastingEnvironment {
    protected EvalSound sound = HexEvalSounds.NOTHING;

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
        return new CircleMishapEnv(this.world, this.impetusLoc, this.startDir);
    }

    @Override
    public EvalSound getSoundType() {
        return sound;
    }

    @Override
    public void postExecution(CastResult result) {
        this.sound = this.sound.greaterOf(result.getSound());
    }

    @Override
    public Vec3 mishapSprayPos() {
        return Vec3.atCenterOf(impetusLoc);
    }

    @Override
    public long extractMedia(long cost) {
        var entity = this.getCircle();
        if (entity == null)
            return cost;

        var mediaAvailable = entity.getMedia();
        if (mediaAvailable < 0)
            return 0;

        long mediaToTake = Math.min(cost, mediaAvailable);
        cost -= mediaToTake;
        entity.setMedia(mediaAvailable - mediaToTake);

        return cost;
    }

    @Override
    public boolean isVecInRange(Vec3 vec) {
        return false;
    }

    @Override
    public InteractionHand castingHand() {
        return InteractionHand.MAIN_HAND;
    }

    @Override
    public ItemStack getAlternateItem() {
        return ItemStack.EMPTY.copy();
    }

    @Override
    protected List<ItemStack> getUsableStacks(StackDiscoveryMode mode) {
        return new ArrayList<>(); // TODO: Could do something like get items in inventories adjacent to the circle?
    }

    @Override
    public FrozenColorizer getColorizer() {
        return new FrozenColorizer(new ItemStack(HexItems.DYE_COLORIZERS.get(DyeColor.PURPLE)), Util.NIL_UUID);
    }

    @Override
    public void produceParticles(ParticleSpray particles, FrozenColorizer colorizer) {
        particles.sprayParticles(this.world, colorizer);
    }
}
