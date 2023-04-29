package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv.SENTINEL_RADIUS;

public class CircleCastEnv extends CastingEnvironment {
    protected final CircleExecutionState execState;

    public CircleCastEnv(ServerLevel world, CircleExecutionState execState) {
        super(world);
        this.execState = execState;
    }

    @Override
    public @Nullable ServerPlayer getCaster() {
        return this.execState.getCaster(this.world);
    }

    public @Nullable BlockEntityAbstractImpetus getImpetus() {
        var entity = this.world.getBlockEntity(execState.impetusPos);

        if (entity instanceof BlockEntityAbstractImpetus)
            return (BlockEntityAbstractImpetus) entity;
        return null;
    }

    public CircleExecutionState circleState() {
        return execState;
    }

    @Override
    public MishapEnvironment getMishapEnvironment() {
        return new CircleMishapEnv(this.world, this.execState);
    }

    @Override
    public void postExecution(CastResult result) {
        // we always want to play this sound one at a time
        var sound = result.getSound().sound();
        if (sound != null) {
            var soundPos = this.execState.currentPos;
            this.world.playSound(null, soundPos, sound, SoundSource.PLAYERS, 1f, 1f);
        }
    }

    @Override
    public Vec3 mishapSprayPos() {
        return Vec3.atCenterOf(this.execState.currentPos);
    }

    @Override
    public long extractMedia(long cost) {
        var entity = this.getImpetus();
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
        var caster = this.execState.getCaster(this.world);
        if (caster != null) {
            var sentinel = HexAPI.instance().getSentinel(caster);
            if (sentinel != null
                && sentinel.extendsRange()
                && caster.getLevel().dimension() == sentinel.dimension()
                && vec.distanceToSqr(sentinel.position()) <= SENTINEL_RADIUS * SENTINEL_RADIUS
            ) {
                return true;
            }
        }

        return this.execState.bounds.contains(vec);
    }

    @Override
    public boolean hasEditPermissionsAt(BlockPos vec) {
        return true;
    }

    @Override
    public InteractionHand getCastingHand() {
        return InteractionHand.MAIN_HAND;
    }

    @Override
    public ItemStack getAlternateItem() {
        return ItemStack.EMPTY.copy(); // TODO: adjacent inventory/item frame?
    }

    @Override
    protected List<ItemStack> getUsableStacks(StackDiscoveryMode mode) {
        return new ArrayList<>(); // TODO: Could do something like get items in inventories adjacent to the circle?
    }

    @Override
    protected List<HeldItemInfo> getPrimaryStacks() {
        return List.of(); // TODO: Adjacent inv!
    }

    @Override
    public FrozenPigment getColorizer() {
        var out = this.getColorizerFromImpetus();
        if (out != null)
            return out;

        // TODO: colouriser from an adjacent inventory also?
        return new FrozenPigment(new ItemStack(HexItems.DYE_COLORIZERS.get(DyeColor.PURPLE)), Util.NIL_UUID);
    }

    private @Nullable FrozenPigment getColorizerFromImpetus() {
        var impetus = this.getImpetus();
        if (impetus == null)
            return null;
        var state = impetus.getExecutionState();
        if (state == null)
            return null;
        return state.colorizer;
    }

    @Override
    public void produceParticles(ParticleSpray particles, FrozenPigment colorizer) {
        particles.sprayParticles(this.world, colorizer);
    }
}
