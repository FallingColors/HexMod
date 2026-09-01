package at.petrak.hexcasting.common.effects;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexDamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Applied when the casting grid is panned too far. During the first second of duration (ie continuously, so long as
 * the effect is constantly being topped up) the player slowly loses oxygen. Once the grid is closed or panned back
 * to center, the effect can start to tick down, and the oxygen drain stops.
 * <br><br>
 * Make sure to use the provided constants for effect duration, or the oxygen drain system may behave oddly.
 */
public class DissociationEffect extends MobEffect {
    public static final int AMP_0_DURATION = 300;
    public static final int AMP_1_DURATION = 600;

    public DissociationEffect() {
        super(MobEffectCategory.HARMFUL, 0x8932b8);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, HexAPI.modLoc("dissociation.slow"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, HexAPI.modLoc("dissociation.fatigue.a"), -0.45, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, HexAPI.modLoc("dissociation.fatigue.b"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, HexAPI.modLoc("dissociation.weak"), -4.0, AttributeModifier.Operation.ADD_VALUE);
    }

    public static boolean shouldPreventBreathing(int duration, int amplifier) {
        return switch (amplifier) {
            case 0 -> duration > AMP_0_DURATION - 20;
            case 1 -> duration > AMP_1_DURATION - 20;
            default -> false;
        };
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (((Player)entity).getAbilities().invulnerable) return true;
        entity.setAirSupply(entity.getAirSupply() - 1);
        if (entity.getAirSupply() == -4) {
            entity.setAirSupply(0);
            entity.hurt(entity.damageSources().source(HexDamageTypes.FORGOT_TO_BREATHE), 2.0F);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        if (!shouldPreventBreathing(duration, amplifier)) return false;
        return switch (amplifier) {
            case 0 -> duration % 10 == 0;
            case 1 -> duration % 5 == 0;
            default -> false;
        };
    }
}
