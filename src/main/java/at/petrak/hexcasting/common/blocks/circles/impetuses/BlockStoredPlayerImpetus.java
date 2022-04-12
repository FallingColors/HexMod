package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.cap.HexCapabilities;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockStoredPlayerImpetus extends BlockAbstractImpetus {
    public BlockStoredPlayerImpetus(Properties p_49795_) {
        super(p_49795_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityStoredPlayerImpetus(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
        BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof BlockEntityStoredPlayerImpetus tile) {
            var usedStack = pPlayer.getItemInHand(pHand);
            var datumContainer = usedStack.getCapability(HexCapabilities.DATUM).resolve();
            if (datumContainer.isPresent()) {
                if (pLevel instanceof ServerLevel level) {
                    var stored = datumContainer.get().readDatum(level);
                    if (stored != null && stored.getType() == DatumType.ENTITY) {
                        var entity = (Entity) stored.getPayload();
                        if (entity instanceof Player) {
                            // phew, we got something
                            tile.setPlayer(entity.getUUID());

                            pLevel.playSound(pPlayer, pPos, HexSounds.IMPETUS_STOREDPLAYER_DING.get(), SoundSource.BLOCKS,
                                    1f, 1f);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
        boolean pIsMoving) {
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);

        if (!pLevel.isClientSide()
            && pLevel.hasNeighborSignal(pPos)
            && !pState.getValue(ENERGIZED)
            && pLevel.getBlockEntity(pPos) instanceof BlockEntityStoredPlayerImpetus tile) {
            var player = tile.getStoredPlayer();
            if (player instanceof ServerPlayer splayer) {
                // phew
                tile.activateSpellCircle(splayer);
            }
        }
    }
}
