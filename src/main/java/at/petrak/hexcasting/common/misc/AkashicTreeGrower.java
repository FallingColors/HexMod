package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;
import java.util.Random;

public class AkashicTreeGrower extends AbstractTreeGrower {
    public static final AkashicTreeGrower INSTANCE = new AkashicTreeGrower();

    public static final List<Holder<ConfiguredFeature<TreeConfiguration, ?>>> GROWERS = Lists.newArrayList();

    static {
        GROWERS.add(buildTreeFeature(HexBlocks.AKASHIC_LEAVES1.get(), "1"));
        GROWERS.add(buildTreeFeature(HexBlocks.AKASHIC_LEAVES2.get(), "2"));
        GROWERS.add(buildTreeFeature(HexBlocks.AKASHIC_LEAVES3.get(), "3"));
    }

    private static Holder<ConfiguredFeature<TreeConfiguration, ?>> buildTreeFeature(Block leaves, String name) {
        return FeatureUtils.register(HexMod.MOD_ID + ":akashic_tree" + name, Feature.TREE,
            new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(HexBlocks.AKASHIC_LOG.get()),
                // baseHeight, heightRandA, heightRandB
                new FancyTrunkPlacer(5, 5, 3),
                BlockStateProvider.simple(leaves),
                // radius, offset, height
                new FancyFoliagePlacer(ConstantInt.of(1), ConstantInt.of(5), 5),
                // limit, lower size, upper size, minclippedheight
                new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(6))
            ).build());
    }

    @Nullable
    @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random pRandom, boolean pLargeHive) {
        return GROWERS.get(pRandom.nextInt(GROWERS.size()));
    }
}
