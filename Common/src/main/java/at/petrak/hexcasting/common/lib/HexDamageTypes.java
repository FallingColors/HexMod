package at.petrak.hexcasting.common.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexDamageTypes {
    public static final ResourceKey<DamageType> OVERCAST = ResourceKey.create(Registries.DAMAGE_TYPE, modLoc("overcast"));
    public static final ResourceKey<DamageType> SHAME_ON_YOU = ResourceKey.create(Registries.DAMAGE_TYPE, modLoc("overcast"));
}
