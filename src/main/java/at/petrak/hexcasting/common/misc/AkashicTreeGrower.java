package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class AkashicTreeGrower extends AbstractTreeGrower {
    public static Holder<ConfiguredFeature<TreeConfiguration, ?>>[] GROWERS;

    static {
        var leaves = new Block[]{
            HexBlocks.AKASHIC_LEAVES1.get(),
            HexBlocks.AKASHIC_LEAVES2.get(),
            HexBlocks.AKASHIC_LEAVES3.get()
        };
        GROWERS = new Holder[leaves.length];
        for (int i = 0; i < leaves.length; i++) {
            GROWERS[i] = FeatureUtils.register(HexMod.MOD_ID + ":akashic_tree" + (i + 1), Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                    BlockStateProvider.simple(HexBlocks.AKASHIC_LOG.get()),
                    // baseHeight, heightRandA, heightRandB
                    new FancyTrunkPlacer(6, 2, 3),
                    BlockStateProvider.simple(leaves[i]),
                    // radius, offset, height
                    new PineFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), UniformInt.of(5, 6)),
                    new TwoLayersFeatureSize(1, 1, 1)
                ).build());
        }
    }

    @Nullable
    @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random pRandom, boolean pLargeHive) {
        return GROWERS[pRandom.nextInt(GROWERS.length)];
    }
}
