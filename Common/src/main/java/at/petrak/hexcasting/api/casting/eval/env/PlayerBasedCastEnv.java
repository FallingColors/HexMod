package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.mod.HexStatistics;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.common.lib.HexAttributes;
import at.petrak.hexcasting.common.lib.HexDamageTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public abstract class PlayerBasedCastEnv extends CastingEnvironment {
    public static final double DEFAULT_AMBIT_RADIUS = 32.0;
    private double ambitRadius;
    public static final double DEFAULT_SENTINEL_RADIUS = 16.0;
    private double sentinelRadius;

    protected final ServerPlayer caster;
    protected final InteractionHand castingHand;

    protected PlayerBasedCastEnv(ServerPlayer caster, InteractionHand castingHand) {
        super(caster.serverLevel());
        this.caster = caster;
        this.castingHand = castingHand;
        this.ambitRadius = caster.getAttributeValue(HexAttributes.AMBIT_RADIUS);
        this.sentinelRadius = caster.getAttributeValue(HexAttributes.SENTINEL_RADIUS);
    }

    @Override
    public LivingEntity getCastingEntity() {
        return this.caster;
    }

    @Override
    public ServerPlayer getCaster() {
        return this.caster;
    }

    @Override
    public void postExecution(CastResult result) {
        super.postExecution(result);

        for (var sideEffect : result.getSideEffects()) {
            if (sideEffect instanceof OperatorSideEffect.DoMishap doMishap) {
                this.sendMishapMsgToPlayer(doMishap);
            }
        }
        if (this.caster != null){
            double ambitAttribute = this.caster.getAttributeValue(HexAttributes.AMBIT_RADIUS);
            if (this.ambitRadius != ambitAttribute){
                this.ambitRadius = ambitAttribute;
            }
            double sentinelAttribute = this.caster.getAttributeValue(HexAttributes.SENTINEL_RADIUS);
            if (this.sentinelRadius != sentinelAttribute){
                this.sentinelRadius = sentinelAttribute;
            }
        }
    }

    @Override
    protected List<ItemStack> getUsableStacks(StackDiscoveryMode mode) {
        return getUsableStacksForPlayer(mode, castingHand, caster);
    }

    @Override
    protected List<HeldItemInfo> getPrimaryStacks() {
        return getPrimaryStacksForPlayer(this.castingHand, this.caster);
    }

    public double getAmbitRadius() {
        return this.ambitRadius;
    }

    public double getSentinelRadius(){
        return this.sentinelRadius;
    }

    @Override
    public boolean replaceItem(Predicate<ItemStack> stackOk, ItemStack replaceWith, @Nullable InteractionHand hand) {
        return replaceItemForPlayer(stackOk, replaceWith, hand, this.caster);
    }

    @Override
    public boolean isVecInRangeEnvironment(Vec3 vec) {
        var sentinel = HexAPI.instance().getSentinel(this.caster);
        if (sentinel != null
            && sentinel.extendsRange()
            && this.caster.level().dimension() == sentinel.dimension()
                // adding 0.00000000001 to avoid machine precision errors at specific angles
                && vec.distanceToSqr(sentinel.position()) <= sentinelRadius * sentinelRadius + 0.00000000001
        ) {
            return true;
        }

        return vec.distanceToSqr(this.caster.position()) <= ambitRadius * ambitRadius + 0.00000000001;
    }

    @Override
    public boolean hasEditPermissionsAtEnvironment(BlockPos pos) {
        return this.caster.gameMode.getGameModeForPlayer() != GameType.ADVENTURE && this.world.mayInteract(this.caster, pos);
    }

    /**
     * Search the player's inventory for media ADs and use them.
     */
    protected long extractMediaFromInventory(long costLeft, boolean allowOvercast, boolean simulate) {
        List<ADMediaHolder> sources = MediaHelper.scanPlayerForMediaStuff(this.caster);

        var startCost = costLeft;

        for (var source : sources) {
            var found = MediaHelper.extractMedia(source, costLeft, false, simulate);
            costLeft -= found;
            if (costLeft <= 0) {
                break;
            }
        }

        if (costLeft > 0 && allowOvercast) {
            double mediaToHealth = HexConfig.common().mediaToHealthRate();
            double healthToRemove = Math.max(costLeft / mediaToHealth, 0.5);
            if (simulate) {
                long simulatedRemovedMedia = Mth.ceil(Math.min(this.caster.getHealth(), healthToRemove) * mediaToHealth);
                costLeft -= simulatedRemovedMedia;
            } else {
                var mediaAbleToCastFromHP = this.caster.getHealth() * mediaToHealth;

                Mishap.trulyHurt(this.caster, this.caster.damageSources().source(HexDamageTypes.OVERCAST), (float) healthToRemove);

                var actuallyTaken = Mth.ceil(mediaAbleToCastFromHP - (this.caster.getHealth() * mediaToHealth));

                HexAdvancementTriggers.OVERCAST_TRIGGER.trigger(this.caster, actuallyTaken);
                this.caster.awardStat(HexStatistics.MEDIA_OVERCAST, actuallyTaken);

                costLeft -= actuallyTaken;
            }
        }

        if (!simulate) {
            this.caster.awardStat(HexStatistics.MEDIA_USED, (int) (startCost - costLeft));
            HexAdvancementTriggers.SPEND_MEDIA_TRIGGER.trigger(
                    this.caster,
                    startCost - costLeft,
                    costLeft < 0 ? -costLeft : 0
            );
        }

        return costLeft;
    }

    protected boolean canOvercast() {
        var adv = this.world.getServer().getAdvancements().get(modLoc("y_u_no_cast_angy"));
        if(adv != null) {
            var advs = this.caster.getAdvancements();
            return advs.getOrStartProgress(adv).isDone();
        }
        return false;
    }

    @Override
    public @Nullable FrozenPigment setPigment(@Nullable FrozenPigment pigment) {
        return IXplatAbstractions.INSTANCE.setPigment(caster, pigment);
    }

    @Override
    public void produceParticles(ParticleSpray particles, FrozenPigment pigment) {
        particles.sprayParticles(this.world, pigment);
    }

    @Override
    public Vec3 mishapSprayPos() {
        return this.caster.position();
    }

    @Override
    public MishapEnvironment getMishapEnvironment() {
        return new PlayerBasedMishapEnv(this.caster);
    }

    protected void sendMishapMsgToPlayer(OperatorSideEffect.DoMishap mishap) {
        var msg = mishap.getMishap().errorMessageWithName(this, mishap.getErrorCtx());
        if (msg != null) {
            this.caster.sendSystemMessage(msg);
        }
    }

    @Override
    protected boolean isCreativeMode() {
        // not sure what the diff between this and isCreative() is
        return this.caster.getAbilities().instabuild;
    }

    @Override
    public void printMessage(Component message) {
        caster.sendSystemMessage(message);
    }
}
