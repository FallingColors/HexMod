package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.misc.TriPredicate;
import at.petrak.hexcasting.common.lib.HexBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;

public class BlockAkashicFloodfiller extends Block {
    public BlockAkashicFloodfiller(Properties p_49795_) {
        super(p_49795_);
    }

    public @Nullable
    BlockPos getRecordPosition(BlockPos here, BlockState state, Level world) {
        return floodFillFor(here, world,
            (pos, bs, level) -> bs.is(HexBlocks.AKASHIC_RECORD));
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        var recordPos = this.getRecordPosition(pPos, pState, pLevel);
        if (recordPos != null && pLevel.getBlockEntity(recordPos) instanceof BlockEntityAkashicRecord akashic) {
            akashic.removeFloodfillerAt(pPos);
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    public boolean canBeFloodedThrough(BlockPos pos, BlockState state, Level world) {
        return true;
    }

    @Nullable
    public static BlockPos floodFillFor(BlockPos start, Level world,
        TriPredicate<BlockPos, BlockState, Level> isValid, TriPredicate<BlockPos, BlockState, Level> isTarget, int maxRange) {
        var seenBlocks = new HashSet<BlockPos>();
        var todo = new ArrayDeque<BlockPos>();
        todo.add(start);

        while (!todo.isEmpty()) {
            var here = todo.remove();

            for (var dir : Direction.values()) {
                var neighbor = here.relative(dir);

                if (neighbor.distSqr(start) > maxRange * maxRange)
                    continue;

                if (seenBlocks.add(neighbor)) {
                    var bs = world.getBlockState(neighbor);
                    if (isTarget.test(neighbor, bs, world)) {
                        return neighbor;
                    } else if (isValid.test(neighbor, bs, world)) {
                        todo.add(neighbor);
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    public static BlockPos floodFillFor(BlockPos start, Level world,
        TriPredicate<BlockPos, BlockState, Level> isTarget) {
        return floodFillFor(start, world, BlockAkashicFloodfiller::canItBeFloodedThrough, isTarget, 32);
    }

    public static boolean canItBeFloodedThrough(BlockPos pos, BlockState state, Level world) {
        if (!(state.getBlock() instanceof BlockAkashicFloodfiller flooder)) {
            return false;
        }

        return flooder.canBeFloodedThrough(pos, state, world);
    }
}
