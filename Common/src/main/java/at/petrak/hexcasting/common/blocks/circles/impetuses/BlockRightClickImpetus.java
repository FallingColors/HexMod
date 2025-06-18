package at.petrak.hexcasting.common.blocks.circles.impetuses;

import at.petrak.hexcasting.api.block.circle.BlockAbstractImpetus;
import at.petrak.hexcasting.api.casting.circles.BlockEntityAbstractImpetus;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockRightClickImpetus extends BlockAbstractImpetus {
    public BlockRightClickImpetus(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockEntityType<? extends BlockEntityAbstractImpetus> getBlockEntityType() {
        return HexBlockEntities.IMPETUS_RIGHTCLICK_TILE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BlockEntityRightClickImpetus(pPos, pState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isShiftKeyDown()) {
            var tile = level.getBlockEntity(pos);
            if (tile instanceof BlockEntityRightClickImpetus impetus) {
                if (player instanceof ServerPlayer sPlayer) {
//                    impetus.activateSpellCircle(serverPlayer);
                    impetus.startExecution(sPlayer);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
