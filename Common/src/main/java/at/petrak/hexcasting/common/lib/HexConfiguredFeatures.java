package at.petrak.hexcasting.common.lib;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;

public class HexConfiguredFeatures {

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(AMETHYST_EDIFIED_TREE, new ConfiguredFeature<>(
                Feature.TREE,
                HexFeatureConfigs.AMETHYST_EDIFIED_TREE_CONFIG
        ));
        context.register(AVENTURINE_EDIFIED_TREE, new ConfiguredFeature<>(
                Feature.TREE,
                HexFeatureConfigs.AVENTURINE_EDIFIED_TREE_CONFIG
        ));
        context.register(CITRINE_EDIFIED_TREE, new ConfiguredFeature<>(
                Feature.TREE,
                HexFeatureConfigs.CITRINE_EDIFIED_TREE_CONFIG
        ));
    }

    public static final ResourceKey<ConfiguredFeature<?, ?>> AMETHYST_EDIFIED_TREE = FeatureUtils.createKey("amethyst_edified_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> AVENTURINE_EDIFIED_TREE = FeatureUtils.createKey("aventurine_edified_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CITRINE_EDIFIED_TREE = FeatureUtils.createKey("citrine_edified_tree");
}
