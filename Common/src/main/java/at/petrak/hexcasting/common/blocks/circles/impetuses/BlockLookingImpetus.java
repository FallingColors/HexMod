package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityLookingImpetus;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockLookingImpetus extends BlockAbstractImpetus {

    public BlockLookingImpetus(Properties p_49795_) {
        super(p_49795_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityLookingImpetus(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState,
        BlockEntityType<T> type) {
        if (!pLevel.isClientSide) {
            return createTickerHelper(type, HexBlockEntities.IMPETUS_LOOK_TILE,
                BlockEntityLookingImpetus::serverTick);
        } else {
            return null;
        }
    }

    // uegh
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
        return p_152134_ == p_152133_ ? (BlockEntityTicker<A>) p_152135_ : null;
    }
}
