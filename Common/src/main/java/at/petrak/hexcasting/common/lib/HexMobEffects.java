package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.misc.HexMobEffect;
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
        .addAttributeModifier(HexAttributes.GRID_ZOOM, "d4afaf0f-df37-4253-9fa7-029e8e4415d9",
            0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
    public static final MobEffect SHRINK_GRID = make("shrink_grid",
        new HexMobEffect(MobEffectCategory.HARMFUL, 0xebad1c))
        .addAttributeModifier(HexAttributes.GRID_ZOOM, "1ce492a9-8bf5-4091-a482-c6d9399e448a",
            -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);


    private static <T extends MobEffect> T make(String id, T effect) {
        var old = EFFECTS.put(modLoc(id), effect);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return effect;
    }
}
