package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.spell.iota.EntityIota;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityStoredPlayerImpetus;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BlockStoredPlayerImpetus extends BlockAbstractImpetus {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BlockStoredPlayerImpetus(Properties p_49795_) {
        super(p_49795_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityStoredPlayerImpetus(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
        BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof BlockEntityStoredPlayerImpetus tile) {
            var usedStack = pPlayer.getItemInHand(pHand);
            var datumContainer = IXplatAbstractions.INSTANCE.findDataHolder(usedStack);
            if (datumContainer != null) {
                if (pLevel instanceof ServerLevel level) {
                    var stored = datumContainer.readIota(level);
                    if (stored instanceof EntityIota eieio) {
                        var entity = eieio.getEntity();
                        if (entity instanceof Player player) {
                            // phew, we got something
                            tile.setPlayer(player.getGameProfile(), entity.getUUID());
                            level.sendBlockUpdated(pPos, pState, pState, Block.UPDATE_CLIENTS);

                            pLevel.playSound(pPlayer, pPos, HexSounds.IMPETUS_STOREDPLAYER_DING,
                                SoundSource.BLOCKS, 1f, 1f);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        super.tick(pState, pLevel, pPos, pRandom);
        if (pLevel.getBlockEntity(pPos) instanceof BlockEntityStoredPlayerImpetus tile) {
            tile.updatePlayerProfile();
        }
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
        boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);

        if (!pLevel.isClientSide()) {
            boolean prevPowered = pState.getValue(POWERED);
            boolean isPowered = pLevel.hasNeighborSignal(pPos);

            if (prevPowered != isPowered) {
                pLevel.setBlockAndUpdate(pPos, pState.setValue(POWERED, isPowered));

                if (isPowered && pLevel.getBlockEntity(pPos) instanceof BlockEntityStoredPlayerImpetus tile) {
                    var player = tile.getStoredPlayer();
                    if (player instanceof ServerPlayer splayer) {
                        // phew
                        tile.activateSpellCircle(splayer);
                    }
                }
            }
        }
    }
}
