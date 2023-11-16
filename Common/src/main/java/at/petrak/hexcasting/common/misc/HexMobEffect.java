package at.petrak.hexcasting.common.misc;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Dodge protected ctor
 */
public class HexMobEffect extends MobEffect {
    public HexMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
