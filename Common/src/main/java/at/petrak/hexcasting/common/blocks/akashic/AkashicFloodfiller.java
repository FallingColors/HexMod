package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.misc.TriPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;

public interface AkashicFloodfiller {
    default boolean canBeFloodedThrough(BlockPos pos, BlockState state, Level world) {
        return true;
    }

    @Nullable
    static BlockPos floodFillFor(BlockPos start, Level world, TriPredicate<BlockPos, BlockState, Level> isTarget) {
        return floodFillFor(start, world, 0f, isTarget);
    }

    @Nullable
    static BlockPos floodFillFor(BlockPos start, Level world, float skipChance,
        TriPredicate<BlockPos, BlockState, Level> isTarget) {
        var seenBlocks = new HashSet<BlockPos>();
        var todo = new ArrayDeque<BlockPos>();
        todo.add(start);
        var skippedBlocks = new HashSet<BlockPos>();

        while (!todo.isEmpty()) {
            var here = todo.remove();

            for (var dir : Direction.values()) {
                var neighbor = here.relative(dir);
                if (seenBlocks.add(neighbor)) {
                    var bs = world.getBlockState(neighbor);
                    if (isTarget.test(neighbor, bs, world)) {
                        if (world.random.nextFloat() > skipChance) {
                            return neighbor;
                        } else {
                            skippedBlocks.add(neighbor);
                        }
                    }
                    if (canItBeFloodedThrough(neighbor, bs, world)) {
                        todo.add(neighbor);
                    }
                }
            }
        }

        if (!skippedBlocks.isEmpty()) {
            // We found something valid, we just skipped past it
            return skippedBlocks.iterator().next();
        }

        return null;
    }

    static boolean canItBeFloodedThrough(BlockPos pos, BlockState state, Level world) {
        if (!(state.getBlock() instanceof AkashicFloodfiller flooder)) {
            return false;
        }

        return flooder.canBeFloodedThrough(pos, state, world);
    }
}
