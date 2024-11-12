package at.petrak.hexcasting.common.lib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;
import at.petrak.hexcasting.mixin.accessor.AccessorPotionBrewing;

public class HexPotions {
    public static void register(BiConsumer<Potion, ResourceLocation> r) {
        for (var e : POTIONS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
        HexPotions.addRecipes();
    }

    private static final Map<ResourceLocation, Potion> POTIONS = new LinkedHashMap<>();

    public static final Potion ENLARGE_GRID = make("enlarge_grid",
        new Potion("enlarge_grid", new MobEffectInstance(HexMobEffects.ENLARGE_GRID, 3600)));
    public static final Potion ENLARGE_GRID_LONG = make("enlarge_grid_long",
        new Potion("enlarge_grid_long", new MobEffectInstance(HexMobEffects.ENLARGE_GRID, 9600)));
    public static final Potion ENLARGE_GRID_STRONG = make("enlarge_grid_strong",
        new Potion("enlarge_grid_strong", new MobEffectInstance(HexMobEffects.ENLARGE_GRID, 1800, 1)));

    public static final Potion SHRINK_GRID = make("shrink_grid",
        new Potion("shrink_grid", new MobEffectInstance(HexMobEffects.SHRINK_GRID, 3600)));
    public static final Potion SHRINK_GRID_LONG = make("shrink_grid_long",
        new Potion("shrink_grid_long", new MobEffectInstance(HexMobEffects.SHRINK_GRID, 9600)));
    public static final Potion SHRINK_GRID_STRONG = make("shrink_grid_strong",
        new Potion("shrink_grid_strong", new MobEffectInstance(HexMobEffects.SHRINK_GRID, 1800, 1)));

    public static void addRecipes() {
        AccessorPotionBrewing.addMix(Potions.AWKWARD, HexItems.AMETHYST_DUST, ENLARGE_GRID);
        AccessorPotionBrewing.addMix(ENLARGE_GRID, Items.REDSTONE, ENLARGE_GRID_LONG);
        AccessorPotionBrewing.addMix(ENLARGE_GRID, Items.GLOWSTONE_DUST, ENLARGE_GRID_STRONG);

        AccessorPotionBrewing.addMix(ENLARGE_GRID, Items.FERMENTED_SPIDER_EYE, SHRINK_GRID);
        AccessorPotionBrewing.addMix(ENLARGE_GRID_LONG, Items.FERMENTED_SPIDER_EYE, SHRINK_GRID_LONG);
        AccessorPotionBrewing.addMix(ENLARGE_GRID_STRONG, Items.FERMENTED_SPIDER_EYE, SHRINK_GRID_STRONG);

        AccessorPotionBrewing.addMix(SHRINK_GRID, Items.REDSTONE, SHRINK_GRID_LONG);
        AccessorPotionBrewing.addMix(SHRINK_GRID, Items.GLOWSTONE_DUST, SHRINK_GRID_STRONG);
    }

    private static <T extends Potion> T make(String id, T potion) {
        var old = POTIONS.put(modLoc(id), potion);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return potion;
    }
}
