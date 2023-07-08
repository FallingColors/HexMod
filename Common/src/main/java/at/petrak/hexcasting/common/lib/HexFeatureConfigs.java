package at.petrak.hexcasting.common.lib;

import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;

import java.util.OptionalInt;

public class HexFeatureConfigs {

    public static final TreeConfiguration AMETHYST_EDIFIED_TREE_CONFIG = akashicTree(HexBlocks.AMETHYST_EDIFIED_LEAVES, HexBlocks.EDIFIED_LOG_AMETHYST);
    public static final TreeConfiguration AVENTURINE_EDIFIED_TREE_CONFIG = akashicTree(HexBlocks.AVENTURINE_EDIFIED_LEAVES, HexBlocks.EDIFIED_LOG_AVENTURINE);
    public static final TreeConfiguration CITRINE_EDIFIED_TREE_CONFIG = akashicTree(HexBlocks.CITRINE_EDIFIED_LEAVES, HexBlocks.EDIFIED_LOG_CITRINE);

    private static TreeConfiguration akashicTree(Block leaves, Block altLog) {
        return new TreeConfiguration.TreeConfigurationBuilder(
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
        ).build();
    }
}
