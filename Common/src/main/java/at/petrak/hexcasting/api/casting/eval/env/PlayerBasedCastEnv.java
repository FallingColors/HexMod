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
import at.petrak.hexcasting.common.lib.HexDamageTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.BlockPos;
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
    public static final double AMBIT_RADIUS = 32.0;
    public static final double SENTINEL_RADIUS = 16.0;

    protected final ServerPlayer caster;
    protected final InteractionHand castingHand;

    protected PlayerBasedCastEnv(ServerPlayer caster, InteractionHand castingHand) {
        super(caster.serverLevel());
        this.caster = caster;
        this.castingHand = castingHand;
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
    }

    @Override
    protected List<ItemStack> getUsableStacks(StackDiscoveryMode mode) {
        return switch (mode) {
            case QUERY -> {
                var out = new ArrayList<ItemStack>();

                var offhand = this.caster.getItemInHand(HexUtils.otherHand(this.castingHand));
                if (!offhand.isEmpty()) {
                    out.add(offhand);
                }

                // If we're casting from the main hand, try to pick from the slot one to the right of the selected slot
                // Otherwise, scan the hotbar left to right
                var anchorSlot = this.castingHand == InteractionHand.MAIN_HAND
                    ? (this.caster.getInventory().selected + 1) % 9
                    : 0;


                for (int delta = 0; delta < 9; delta++) {
                    var slot = (anchorSlot + delta) % 9;
                    out.add(this.caster.getInventory().getItem(slot));
                }

                yield out;
            }
            case EXTRACTION -> {
                // https://wiki.vg/Inventory is WRONG
                // slots 0-8 are the hotbar
                // for what purpose i cannot imagine
                // http://redditpublic.com/images/b/b2/Items_slot_number.png looks right
                // and offhand is 150 Inventory.java:464
                var out = new ArrayList<ItemStack>();

                // First, the inventory backwards
                // We use inv.items here to get the main inventory, but not offhand or armor
                Inventory inv = this.caster.getInventory();
                for (int i = inv.items.size() - 1; i >= 0; i--) {
                    if (i != inv.selected) {
                        out.add(inv.items.get(i));
                    }
                }

                // then the offhand, then the selected hand
                out.addAll(inv.offhand);
                out.add(inv.getSelected());

                yield out;
            }
        };
    }

    @Override
    protected List<HeldItemInfo> getPrimaryStacks() {
        var primaryItem = this.caster.getItemInHand(this.castingHand);

        if (primaryItem.isEmpty())
            primaryItem = ItemStack.EMPTY.copy();

        return List.of(new HeldItemInfo(getAlternateItem(), this.getOtherHand()), new HeldItemInfo(primaryItem,
            this.castingHand));
    }

    ItemStack getAlternateItem() {
        var otherHand = HexUtils.otherHand(this.castingHand);
        var stack = this.caster.getItemInHand(otherHand);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY.copy();
        } else {
            return stack;
        }
    }

    @Override
    public boolean replaceItem(Predicate<ItemStack> stackOk, ItemStack replaceWith, @Nullable InteractionHand hand) {
        if (caster == null)
            return false;

        if (hand != null && stackOk.test(caster.getItemInHand(hand))) {
            caster.setItemInHand(hand, replaceWith);
            return true;
        }

        Inventory inv = this.caster.getInventory();
        for (int i = inv.items.size() - 1; i >= 0; i--) {
            if (i != inv.selected) {
                if (stackOk.test(inv.items.get(i))) {
                    inv.setItem(i, replaceWith);
                    return true;
                }
            }
        }

        if (stackOk.test(caster.getItemInHand(getOtherHand()))) {
            caster.setItemInHand(getOtherHand(), replaceWith);
            return true;
        }
        if (stackOk.test(caster.getItemInHand(getCastingHand()))) {
            caster.setItemInHand(getCastingHand(), replaceWith);
            return true;
        }

        return false;
    }

    @Override
    public boolean isVecInRangeEnvironment(Vec3 vec) {
        var sentinel = HexAPI.instance().getSentinel(this.caster);
        if (sentinel != null
            && sentinel.extendsRange()
            && this.caster.level().dimension() == sentinel.dimension()
            && vec.distanceToSqr(sentinel.position()) <= SENTINEL_RADIUS * SENTINEL_RADIUS
        ) {
            return true;
        }

        return vec.distanceToSqr(this.caster.position()) <= AMBIT_RADIUS * AMBIT_RADIUS;
    }

    @Override
    public boolean hasEditPermissionsAtEnvironment(BlockPos pos) {
        return this.caster.gameMode.getGameModeForPlayer() != GameType.ADVENTURE && this.world.mayInteract(this.caster, pos);
    }

    /**
     * Search the player's inventory for media ADs and use them.
     */
    protected long extractMediaFromInventory(long costLeft, boolean allowOvercast) {
        List<ADMediaHolder> sources = MediaHelper.scanPlayerForMediaStuff(this.caster);

        var startCost = costLeft;

        for (var source : sources) {
            var found = MediaHelper.extractMedia(source, costLeft, false, false);
            costLeft -= found;
            if (costLeft <= 0) {
                break;
            }
        }

        if (costLeft > 0 && allowOvercast) {
            double mediaToHealth = HexConfig.common().mediaToHealthRate();
            double healthToRemove = Math.max(costLeft / mediaToHealth, 0.5);
            var mediaAbleToCastFromHP = this.caster.getHealth() * mediaToHealth;

            Mishap.trulyHurt(this.caster, this.caster.damageSources().source(HexDamageTypes.OVERCAST), (float) healthToRemove);

            var actuallyTaken = Mth.ceil(mediaAbleToCastFromHP - (this.caster.getHealth() * mediaToHealth));

            HexAdvancementTriggers.OVERCAST_TRIGGER.trigger(this.caster, actuallyTaken);
            this.caster.awardStat(HexStatistics.MEDIA_OVERCAST, actuallyTaken);

            costLeft -= actuallyTaken;
        }

        this.caster.awardStat(HexStatistics.MEDIA_USED, (int) (startCost - costLeft));
        HexAdvancementTriggers.SPEND_MEDIA_TRIGGER.trigger(
            this.caster,
            startCost - costLeft,
            costLeft < 0 ? -costLeft : 0
        );

        return costLeft;
    }

    protected boolean canOvercast() {
        var adv = this.world.getServer().getAdvancements().getAdvancement(modLoc("y_u_no_cast_angy"));
        var advs = this.caster.getAdvancements();
        return advs.getOrStartProgress(adv).isDone();
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
