package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.common.lib.HexConfiguredFeatures;
import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.List;

public class AkashicTreeGrower extends AbstractTreeGrower {
    public static final AkashicTreeGrower INSTANCE = new AkashicTreeGrower();

    public static final List<ResourceKey<ConfiguredFeature<?, ?>>> GROWERS = Lists.newArrayList();

    public static void init() {
        GROWERS.add(HexConfiguredFeatures.AMETHYST_EDIFIED_TREE);
        GROWERS.add(HexConfiguredFeatures.AVENTURINE_EDIFIED_TREE);
        GROWERS.add(HexConfiguredFeatures.CITRINE_EDIFIED_TREE);
    }

    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean pLargeHive) {
        return GROWERS.get(pRandom.nextInt(GROWERS.size()));
    }
}
