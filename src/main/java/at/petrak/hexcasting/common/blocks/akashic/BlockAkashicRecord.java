package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.api.spell.DatumType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;

public class BlockAkashicRecord extends Block implements EntityBlock {
    public BlockAkashicRecord(Properties p_49795_) {
        super(p_49795_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityAkashicRecord(pPos, pState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof BlockEntityAkashicRecord record) {
            return Math.min(15, record.getCount());
        }
        return 0;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        var seen = new HashSet<BlockPos>();
        var todo = new ArrayDeque<BlockPos>();
        todo.add(pPos);
        // we do NOT add this position to the valid positions, because the record
        // isn't flood-fillable through.
        while (!todo.isEmpty()) {
            var here = todo.remove();

            for (var dir : Direction.values()) {
                var neighbor = here.relative(dir);
                if (seen.add(neighbor)) {
                    var bs = pLevel.getBlockState(neighbor);
                    if (BlockAkashicFloodfiller.canItBeFloodedThrough(neighbor, bs, pLevel)) {
                        todo.add(neighbor);
                    }
                    if (pLevel.getBlockEntity(neighbor) instanceof BlockEntityAkashicBookshelf shelf) {
                        shelf.setNewData(null, null, DatumType.EMPTY);
                    }
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
