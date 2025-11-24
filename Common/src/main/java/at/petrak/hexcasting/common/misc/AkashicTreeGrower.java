package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.common.lib.HexConfiguredFeatures;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.List;

// i gotcha :3
public class AkashicTreeGrower {
    public static final AkashicTreeGrower INSTANCE = new AkashicTreeGrower();

    public static final List<ResourceKey<ConfiguredFeature<?, ?>>> GROWERS = Lists.newArrayList();

    public static void init() {
        GROWERS.add(HexConfiguredFeatures.AMETHYST_EDIFIED_TREE);
        GROWERS.add(HexConfiguredFeatures.AVENTURINE_EDIFIED_TREE);
        GROWERS.add(HexConfiguredFeatures.CITRINE_EDIFIED_TREE);
    }

    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom) {
        return GROWERS.get(pRandom.nextInt(GROWERS.size()));
    }

    public boolean growTree(ServerLevel level, ChunkGenerator generator, BlockPos pos, RandomSource source) {
        if (GROWERS.isEmpty()) return false;

        var key = this.getConfiguredFeature(source);

        var registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);

        var holder = registry.getHolderOrThrow(key);
        ConfiguredFeature<?, ?> feature = holder.value();

        return feature.place(level, generator, source, pos);
    }
}
