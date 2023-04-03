package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.HexAPI;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv.SENTINEL_RADIUS;

public class CircleCastEnv extends CastingEnvironment {
    protected EvalSound sound = HexEvalSounds.NOTHING;

    protected final BlockPos impetusLoc;
    protected final Direction startDir;
    protected final @Nullable ServerPlayer caster;
    protected final AABB bounds;

    public CircleCastEnv(ServerLevel world, BlockPos impetusLoc, Direction startDir, @Nullable ServerPlayer caster, AABB bounds) {
        super(world);
        this.impetusLoc = impetusLoc;
        this.startDir = startDir;
        this.caster = caster;
        this.bounds = bounds;
    }

    @Override
    public @Nullable ServerPlayer getCaster() {
        return this.caster;
    }

    public @Nullable BlockEntityAbstractImpetus getImpetus() {
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
        return new CircleMishapEnv(this.world, this.impetusLoc, this.startDir, this.caster, this.bounds);
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
        if (this.caster != null) {
            var sentinel = HexAPI.instance().getSentinel(this.caster);
            if (sentinel != null
                    && sentinel.extendsRange()
                    && this.caster.getLevel().dimension() == sentinel.dimension()
                    && vec.distanceToSqr(sentinel.position()) <= SENTINEL_RADIUS * SENTINEL_RADIUS
            ) {
                return true;
            }
        }

        return this.bounds.contains(vec);
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
    public FrozenColorizer getColorizer() {
        var out = this.getColorizerFromImpetus();
        if (out != null)
            return out;

        // TODO: colouriser from an adjacent inventory also?
        return new FrozenColorizer(new ItemStack(HexItems.DYE_COLORIZERS.get(DyeColor.PURPLE)), Util.NIL_UUID);
    }

    private @Nullable FrozenColorizer getColorizerFromImpetus() {
        var impetus = this.getImpetus();
        if (impetus == null)
            return null;
        var state = impetus.getExecutionState();
        if (state == null)
            return null;
        return state.colorizer;
    }

    @Override
    public void produceParticles(ParticleSpray particles, FrozenColorizer colorizer) {
        particles.sprayParticles(this.world, colorizer);
    }
}
