package at.petrak.hexcasting.common.lib;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexPotions {
    private static final Map<ResourceLocation, Potion> POTIONS = new LinkedHashMap<>();

    public static final Potion ENLARGE_GRID = make("enlarge_grid",
        new Potion("enlarge_grid", new MobEffectInstance(Holder.direct(HexMobEffects.ENLARGE_GRID), 3600)));
    public static final Potion ENLARGE_GRID_LONG = make("enlarge_grid_long",
        new Potion("enlarge_grid_long", new MobEffectInstance(Holder.direct(HexMobEffects.ENLARGE_GRID), 9600)));
    public static final Potion ENLARGE_GRID_STRONG = make("enlarge_grid_strong",
        new Potion("enlarge_grid_strong", new MobEffectInstance(Holder.direct(HexMobEffects.ENLARGE_GRID), 1800, 1)));

    public static final Potion SHRINK_GRID = make("shrink_grid",
        new Potion("shrink_grid", new MobEffectInstance(Holder.direct(HexMobEffects.SHRINK_GRID), 3600)));
    public static final Potion SHRINK_GRID_LONG = make("shrink_grid_long",
        new Potion("shrink_grid_long", new MobEffectInstance(Holder.direct(HexMobEffects.SHRINK_GRID), 9600)));
    public static final Potion SHRINK_GRID_STRONG = make("shrink_grid_strong",
        new Potion("shrink_grid_strong", new MobEffectInstance(Holder.direct(HexMobEffects.SHRINK_GRID), 1800, 1)));

    public static void registerPotions(BiConsumer<Potion, ResourceLocation> r) {
        for (var e : POTIONS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    public static void addRecipes(PotionBrewing.Builder builder, RegistryAccess registryAccess) {
        var potionRegistry = registryAccess.registryOrThrow(Registries.POTION);

        builder.addMix(Potions.AWKWARD, HexItems.AMETHYST_DUST, potionRegistry.wrapAsHolder(ENLARGE_GRID));
        builder.addMix(potionRegistry.wrapAsHolder(ENLARGE_GRID), Items.REDSTONE, potionRegistry.wrapAsHolder(ENLARGE_GRID_LONG));
        builder.addMix(potionRegistry.wrapAsHolder(ENLARGE_GRID), Items.GLOWSTONE_DUST, potionRegistry.wrapAsHolder(ENLARGE_GRID_STRONG));

        builder.addMix(potionRegistry.wrapAsHolder(ENLARGE_GRID), Items.FERMENTED_SPIDER_EYE, potionRegistry.wrapAsHolder(SHRINK_GRID));
        builder.addMix(potionRegistry.wrapAsHolder(ENLARGE_GRID_LONG), Items.FERMENTED_SPIDER_EYE, potionRegistry.wrapAsHolder(SHRINK_GRID_LONG));
        builder.addMix(potionRegistry.wrapAsHolder(ENLARGE_GRID_STRONG), Items.FERMENTED_SPIDER_EYE, potionRegistry.wrapAsHolder(SHRINK_GRID_STRONG));

        builder.addMix(potionRegistry.wrapAsHolder(SHRINK_GRID), Items.REDSTONE, potionRegistry.wrapAsHolder(SHRINK_GRID_LONG));
        builder.addMix(potionRegistry.wrapAsHolder(SHRINK_GRID), Items.GLOWSTONE_DUST, potionRegistry.wrapAsHolder(SHRINK_GRID_STRONG));
    }

    private static <T extends Potion> T make(String id, T potion) {
        var old = POTIONS.put(modLoc(id), potion);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return potion;
    }
}
