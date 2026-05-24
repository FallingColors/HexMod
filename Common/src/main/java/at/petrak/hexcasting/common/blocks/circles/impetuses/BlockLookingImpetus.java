package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockLookingImpetus extends BlockAbstractImpetus {

    public BlockLookingImpetus(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockEntityType<? extends BlockEntityAbstractImpetus> getBlockEntityType() {
        return HexBlockEntities.IMPETUS_LOOK_TILE;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        return null;
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
    @SuppressWarnings("unchecked")
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> type, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>) ticker : null;
    }
}
