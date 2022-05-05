package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityRightClickImpetus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockRightClickImpetus extends BlockAbstractImpetus {
    public BlockRightClickImpetus(Properties p_49795_) {
        super(p_49795_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityRightClickImpetus(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
        BlockHitResult pHit) {
        if (!pPlayer.isShiftKeyDown()) {
            var tile = pLevel.getBlockEntity(pPos);
            if (tile instanceof BlockEntityRightClickImpetus impetus) {
                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    impetus.activateSpellCircle(serverPlayer);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
