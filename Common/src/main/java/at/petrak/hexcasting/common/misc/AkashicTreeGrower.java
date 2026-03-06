package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.common.lib.HexConfiguredFeatures;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.List;
import java.util.Optional;

public class AkashicTreeGrower {
    public static final List<ResourceKey<ConfiguredFeature<?, ?>>> GROWERS = Lists.newArrayList();

    private static final TreeGrower[] TREE_GROWERS = new TreeGrower[3];

    public static void init() {
        GROWERS.add(HexConfiguredFeatures.AMETHYST_EDIFIED_TREE);
        GROWERS.add(HexConfiguredFeatures.AVENTURINE_EDIFIED_TREE);
        GROWERS.add(HexConfiguredFeatures.CITRINE_EDIFIED_TREE);
        TREE_GROWERS[0] = new TreeGrower("akashic_amethyst", Optional.of(GROWERS.get(0)), Optional.empty(), Optional.empty());
        TREE_GROWERS[1] = new TreeGrower("akashic_aventurine", Optional.of(GROWERS.get(1)), Optional.empty(), Optional.empty());
        TREE_GROWERS[2] = new TreeGrower("akashic_citrine", Optional.of(GROWERS.get(2)), Optional.empty(), Optional.empty());
    }

    public static boolean growTree(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.level.chunk.ChunkGenerator chunkGen,
            net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state,
            RandomSource random) {
        return TREE_GROWERS[random.nextInt(TREE_GROWERS.length)].growTree(level, chunkGen, pos, state, random);
    }
}
