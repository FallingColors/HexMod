package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.casting.eval.MishapEnvironment;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.common.lib.HexDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PlayerBasedMishapEnv extends MishapEnvironment {
    public PlayerBasedMishapEnv(ServerPlayer player) {
        super(player.serverLevel(), player);
    }

    @Override
    public void yeetHeldItemsTowards(Vec3 targetPos) {
        var pos = this.caster.position();
        var delta = targetPos.subtract(pos).normalize().scale(0.5);

        for (var hand : InteractionHand.values()) {
            var stack = this.caster.getItemInHand(hand);
            this.caster.setItemInHand(hand, ItemStack.EMPTY);
            this.yeetItem(stack, pos, delta);
        }
    }

    @Override
    public void dropHeldItems() {
        var delta = this.caster.getLookAngle();
        this.yeetHeldItemsTowards(this.caster.position().add(delta));
    }

    @Override
    public void damage(float healthProportion) {
        Mishap.trulyHurt(this.caster, this.caster.damageSources().source(HexDamageTypes.OVERCAST), this.caster.getHealth() * healthProportion);
    }

    @Override
    public void drown() {
        if (this.caster.getAirSupply() < 200) {
            this.caster.hurt(this.caster.damageSources().drown(), 2f);
        }
        this.caster.setAirSupply(0);
    }

    @Override
    public void removeXp(int amount) {
        this.caster.giveExperiencePoints(-amount);
    }

    @Override
    public void blind(int ticks) {
        this.caster.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ticks));
    }
}
