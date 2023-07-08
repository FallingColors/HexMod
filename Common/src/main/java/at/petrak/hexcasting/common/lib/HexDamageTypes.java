package at.petrak.hexcasting.common.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class HexDamageTypes {
    public static final ResourceKey<DamageType> OVERCAST = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("overcast"));
    public static final ResourceKey<DamageType> SHAME_ON_YOU = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("overcast"));

    public static void bootstrap(BootstapContext<DamageType> context) {
        context.register(OVERCAST, new DamageType("hexcasting.overcast", 0f));
        context.register(SHAME_ON_YOU, new DamageType("hexcasting.shame", 0f));
    }
}
