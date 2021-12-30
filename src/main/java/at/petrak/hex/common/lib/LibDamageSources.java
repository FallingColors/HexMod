package at.petrak.hex.common.lib;

import net.minecraft.world.damagesource.DamageSource;

public class LibDamageSources {
    public static final DamageSource OVERCAST = new DamageSource("hex.overcast").bypassArmor().bypassMagic().setMagic();
}
