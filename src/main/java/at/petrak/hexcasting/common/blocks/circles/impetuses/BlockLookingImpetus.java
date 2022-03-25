package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

public class BlockLookingImpetus extends BlockAbstractImpetus {
    public static final int MAX_LOOK_AMOUNT = 30;
    public static final IntegerProperty LOOK_AMOUNT = IntegerProperty.create("look_amount", 0, MAX_LOOK_AMOUNT);

    public BlockLookingImpetus(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(ENERGIZED, false)
            .setValue(FACING, Direction.NORTH)
            .setValue(LOOK_AMOUNT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LOOK_AMOUNT);
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
            return createTickerHelper(type, HexBlocks.IMPETUS_LOOK_TILE.get(), BlockEntityLookingImpetus::serverTick);
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
