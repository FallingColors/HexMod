package at.petrak.hexcasting.api.utils;

import net.minecraft.world.damagesource.DamageSource;

public final class HexDamageSources {
    public static final DamageSource OVERCAST = new DamageSource("hexcasting.overcast")
        .bypassArmor()
        .bypassMagic()
        .setMagic();
}
