package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.misc.HexMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexMobEffects {
    public static void register(BiConsumer<MobEffect, ResourceLocation> r) {
        for (var e : EFFECTS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, MobEffect> EFFECTS = new LinkedHashMap<>();

    public static final MobEffect ENLARGE_GRID = make("enlarge_grid",
        new HexMobEffect(MobEffectCategory.BENEFICIAL, 0xc875ff))
        .addAttributeModifier(net.minecraft.core.Holder.direct(HexAttributes.GRID_ZOOM), modLoc("enlarge_grid_modifier"),
            0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final MobEffect SHRINK_GRID = make("shrink_grid",
        new HexMobEffect(MobEffectCategory.HARMFUL, 0xc0e660))
        .addAttributeModifier(net.minecraft.core.Holder.direct(HexAttributes.GRID_ZOOM), modLoc("shrink_grid_modifier"),
            -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);


    private static <T extends MobEffect> T make(String id, T effect) {
        var old = EFFECTS.put(modLoc(id), effect);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return effect;
    }

    /** 1.21+ Get Holder for MobEffectInstance; pass level.registryAccess() or entity.registryAccess() */
    public static Holder<MobEffect> getHolder(MobEffect effect, net.minecraft.core.HolderLookup.Provider registryAccess) {
        for (var e : EFFECTS.entrySet()) {
            if (e.getValue() == effect) {
                var key = ResourceKey.create(Registries.MOB_EFFECT, e.getKey());
                return registryAccess.lookupOrThrow(Registries.MOB_EFFECT).getOrThrow(key);
            }
        }
        throw new IllegalArgumentException("Effect " + effect + " not registered in HexMobEffects");
    }
}
