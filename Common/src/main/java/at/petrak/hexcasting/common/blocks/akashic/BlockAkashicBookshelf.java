package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.spell.iota.PatternIota;
import at.petrak.hexcasting.common.items.ItemScroll;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.xplat.IForgeLikeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockAkashicBookshelf extends Block implements AkashicFloodfiller, EntityBlock, IForgeLikeBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_BOOKS = BooleanProperty.create("has_books");

    public BlockAkashicBookshelf(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.getStateDefinition().any()
            .setValue(FACING, Direction.NORTH)
            .setValue(HAS_BOOKS, false));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
        BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof BlockEntityAkashicBookshelf shelf) {
            var stack = pPlayer.getItemInHand(pHand);
            if (stack.getItem() instanceof ItemScroll scroll) {
                if (!pLevel.isClientSide()) {
                    scroll.writeDatum(stack, new PatternIota(shelf.getPattern()));
                }
                pLevel.playSound(pPlayer, pPos, HexSounds.SCROLL_SCRIBBLE, SoundSource.BLOCKS, 1f, 1f);
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            } else if (pPlayer.isDiscrete() && pHand == InteractionHand.MAIN_HAND && stack.isEmpty()) {
                if (!pLevel.isClientSide()) {
                    shelf.clearIota();
                }

                pLevel.playSound(pPlayer, pPos, HexSounds.SCROLL_SCRIBBLE, SoundSource.BLOCKS,
                    1f, 0.8f);
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_BOOKS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @SoftImplement("forge")
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return hasEnchantPowerBonus(state, level, pos) ? 1 : 0;
    }

    @Override
    public boolean hasEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return pState.getValue(HAS_BOOKS);
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return pState.getValue(HAS_BOOKS) ? 15 : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityAkashicBookshelf(pPos, pState);
    }

    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
    }

    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }
}
