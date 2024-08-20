package at.petrak.hexcasting.common.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexDamageTypes {
    public static final ResourceKey<DamageType> OVERCAST = ResourceKey.create(Registries.DAMAGE_TYPE, modLoc("overcast"));
    public static final ResourceKey<DamageType> SHAME_ON_YOU = ResourceKey.create(Registries.DAMAGE_TYPE, modLoc("shame"));

    public static void bootstrap(BootstapContext<DamageType> ctx) {
        ctx.register(OVERCAST, new DamageType(
                "hexcasting.overcast",
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                0f
        ));

        ctx.register(SHAME_ON_YOU, new DamageType(
                "hexcasting.shame",
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                0f
        ));
    }
}
