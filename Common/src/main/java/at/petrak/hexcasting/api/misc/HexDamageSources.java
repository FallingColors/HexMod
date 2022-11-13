package at.petrak.hexcasting.api.misc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public final class HexDamageSources {
    public static final DamageSourceOvercast OVERCAST = new DamageSourceOvercast();
    public static final DamageSourceShameOnYou SHAME = new DamageSourceShameOnYou();

    public static DamageSource overcastDamageFrom(Entity cause) {
        return new EntityDamageSourceOvercast(cause);
    }
}
