package at.petrak.hexcasting.api.utils;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;

public final class HexDamageSources {
    public static final DamageSource OVERCAST = new DamageSource("hexcasting.overcast")
        .bypassArmor()
        .bypassMagic()
        .setMagic();

    public static DamageSource overcastDamageFrom(Entity cause) {
        return new EntityDamageSource("hexcasting.overcast", cause)
            .bypassArmor()
            .bypassMagic()
            .setMagic();
    }
}
