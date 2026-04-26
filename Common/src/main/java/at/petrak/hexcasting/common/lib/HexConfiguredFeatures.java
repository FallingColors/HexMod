package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class HexConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> AMETHYST_EDIFIED_TREE = createKey("amethyst_edified_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AVENTURINE_EDIFIED_TREE = createKey("aventurine_edified_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CITRINE_EDIFIED_TREE = createKey("citrine_edified_tree");

    private static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(HexAPI.MOD_ID, name));
    }
}
