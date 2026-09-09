package at.petrak.hexcasting.common.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexDamageTypes {
    public static final ResourceKey<DamageType> OVERCAST = ResourceKey.create(Registries.DAMAGE_TYPE, modLoc("overcast"));
    public static final ResourceKey<DamageType> FORGOT_TO_BREATHE = ResourceKey.create(Registries.DAMAGE_TYPE, modLoc("forgot_to_breathe"));

    public static void bootstrap(BootstrapContext<DamageType> ctx) {
        ctx.register(OVERCAST, new DamageType(
            "hexcasting.overcast",
            DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
            0f
        ));
        ctx.register(FORGOT_TO_BREATHE, new DamageType(
            "hexcasting.forgot_to_breathe",
            DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
            0f
        ));
    }
}
