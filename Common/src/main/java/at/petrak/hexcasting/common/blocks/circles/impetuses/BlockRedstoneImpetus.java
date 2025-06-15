package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.api.casting.iota.EntityIota;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockRedstoneImpetus extends BlockAbstractImpetus {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BlockRedstoneImpetus(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockEntityType<? extends BlockEntityAbstractImpetus> getBlockEntityType() {
        return HexBlockEntities.IMPETUS_REDSTONE_TILE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityRedstoneImpetus(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level instanceof ServerLevel sLevel
                && level.getBlockEntity(pos) instanceof BlockEntityRedstoneImpetus tile) {
            var usedStack = player.getItemInHand(hand);
            if (usedStack.isEmpty() && player.isDiscrete()) {
                tile.clearPlayer();
                tile.sync();
                level.playSound(null, pos, HexSounds.IMPETUS_REDSTONE_CLEAR, SoundSource.BLOCKS, 1f, 1f);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else {
                var datumContainer = IXplatAbstractions.INSTANCE.findDataHolder(usedStack);
                if (datumContainer != null) {
                    var stored = datumContainer.readIota();
                    if (stored instanceof EntityIota eieio) {
                        var entity = eieio.getOrFindEntity(sLevel);
                        if (entity instanceof Player iotaPlayer) {
                            // phew, we got something
                            tile.setPlayer(iotaPlayer.getGameProfile(), entity.getUUID());
                            tile.sync();

                            level.playSound(null, pos, HexSounds.IMPETUS_REDSTONE_DING,
                                    SoundSource.BLOCKS, 1f, 1f);
                            return ItemInteractionResult.sidedSuccess(level.isClientSide);
                        }
                    }
                }
            }
        }

        return stack.isEmpty() && hand == InteractionHand.MAIN_HAND
                ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        super.tick(pState, pLevel, pPos, pRandom);
        if (pLevel.getBlockEntity(pPos) instanceof BlockEntityRedstoneImpetus tile) {
            tile.updatePlayerProfile();
        }
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
        boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);

        if (pLevel instanceof ServerLevel slevel) {
            boolean prevPowered = pState.getValue(POWERED);
            boolean isPowered = pLevel.hasNeighborSignal(pPos);

            if (prevPowered != isPowered) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(POWERED, isPowered));

                if (isPowered && pLevel.getBlockEntity(pPos) instanceof BlockEntityRedstoneImpetus tile) {
                    var player = tile.getStoredPlayer();
                    tile.startExecution(player);
                }
            }
        }
    }
}
