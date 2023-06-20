package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexBlocks;
import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;

import java.util.List;
import java.util.OptionalInt;

public class AkashicTreeGrower extends AbstractTreeGrower {
    public static final AkashicTreeGrower INSTANCE = new AkashicTreeGrower();

    public static final List<Holder<ConfiguredFeature<TreeConfiguration, ?>>> GROWERS = Lists.newArrayList();

    public static void init() {
        GROWERS.add(buildTreeFeature(HexBlocks.AMETHYST_EDIFIED_LEAVES, HexBlocks.EDIFIED_LOG_AMETHYST, "1"));
        GROWERS.add(buildTreeFeature(HexBlocks.AVENTURINE_EDIFIED_LEAVES, HexBlocks.EDIFIED_LOG_AVENTURINE, "2"));
        GROWERS.add(buildTreeFeature(HexBlocks.CITRINE_EDIFIED_LEAVES, HexBlocks.EDIFIED_LOG_CITRINE, "3"));
    }

    private static Holder<ConfiguredFeature<TreeConfiguration, ?>> buildTreeFeature(BootstapContext<ConfiguredFeature<?, ?>> context, Block leaves, Block altLog, String name) {
        return FeatureUtils.register(context,
                null,
                null,
                new TreeConfiguration.TreeConfigurationBuilder(
                        new WeightedStateProvider(
                                SimpleWeightedRandomList.<BlockState>builder()
                                        .add(HexBlocks.EDIFIED_LOG.defaultBlockState(), 8)
                                        .add(altLog.defaultBlockState(), 1)
                                        .build()),
                        // baseHeight, heightRandA, heightRandB
                        new FancyTrunkPlacer(5, 5, 3),
                        BlockStateProvider.simple(leaves),
                        // radius, offset, height
                        new FancyFoliagePlacer(ConstantInt.of(1), ConstantInt.of(5), 5),
                        // limit, lower size, upper size, minclippedheight
                        new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(6))
                ).build()
        );
    }

    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean pLargeHive) {
        return GROWERS.get(pRandom.nextInt(GROWERS.size()));
    }
}
