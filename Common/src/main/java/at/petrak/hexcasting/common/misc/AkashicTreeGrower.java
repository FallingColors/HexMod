package at.petrak.hexcasting.common.misc;

import at.petrak.hexcasting.common.lib.HexConfiguredFeatures;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.List;

public class AkashicTreeGrower {
    public static final AkashicTreeGrower INSTANCE = new AkashicTreeGrower();

    public static final List<ResourceKey<ConfiguredFeature<?, ?>>> GROWERS = Lists.newArrayList();

    public static void init() {
        GROWERS.add(HexConfiguredFeatures.AMETHYST_EDIFIED_TREE);
        GROWERS.add(HexConfiguredFeatures.AVENTURINE_EDIFIED_TREE);
        GROWERS.add(HexConfiguredFeatures.CITRINE_EDIFIED_TREE);
    }

    private ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean pLargeHive) {
        return GROWERS.get(pRandom.nextInt(GROWERS.size()));
    }

    public boolean growTree(ServerLevel level, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, RandomSource random) {
        ResourceKey<ConfiguredFeature<?, ?>> treeFeatureKey = getConfiguredFeature(random, hasFlowers(level, pos));
        if (treeFeatureKey == null) {
            return false;
        } else {
            Holder<ConfiguredFeature<?, ?>> holder1 = level.registryAccess()
                    .registryOrThrow(Registries.CONFIGURED_FEATURE)
                    .getHolder(treeFeatureKey)
                    .orElse(null);
            var event = net.neoforged.neoforge.event.EventHooks.fireBlockGrowFeature(level, random, pos, holder1);
            holder1 = event.getFeature();
            if (event.isCanceled()) return false;
            if (holder1 == null) {
                return false;
            } else {
                ConfiguredFeature<?, ?> configuredfeature1 = holder1.value();
                BlockState blockstate1 = level.getFluidState(pos).createLegacyBlock();
                level.setBlock(pos, blockstate1, 4);
                if (configuredfeature1.place(level, chunkGenerator, random, pos)) {
                    if (level.getBlockState(pos) == blockstate1) {
                        level.sendBlockUpdated(pos, state, blockstate1, 2);
                    }

                    return true;
                } else {
                    level.setBlock(pos, state, 4);
                    return false;
                }
            }
        }
    }

    private boolean hasFlowers(LevelAccessor level, BlockPos pos) {
        for (BlockPos blockpos : BlockPos.MutableBlockPos.betweenClosed(pos.below().north(2).west(2), pos.above().south(2).east(2))) {
            if (level.getBlockState(blockpos).is(BlockTags.FLOWERS)) {
                return true;
            }
        }

        return false;
    }
}
