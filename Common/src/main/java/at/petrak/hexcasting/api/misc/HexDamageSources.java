package at.petrak.hexcasting.api.misc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public final class HexDamageSources {
    public static final DamageSource OVERCAST = new DamageSourceOvercast();

    public static DamageSource overcastDamageFrom(Entity cause) {
        return new EntityDamageSourceOvercast(cause);
    }
}
