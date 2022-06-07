package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.xplat.IForgeLikeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentTableBlock.class)
public class FabricEnchantmentTableBlockMixin {
    @Inject(method = "isValidBookShelf", at = @At("HEAD"), cancellable = true)
    private static void treatAsBookshelf(Level level, BlockPos blockPos, BlockPos blockPos2, CallbackInfoReturnable<Boolean> cir) {
        BlockState state = level.getBlockState(blockPos.offset(blockPos2));
        if (state.getBlock() instanceof IForgeLikeBlock forgeLike) {
            boolean emptyBetween = level.isEmptyBlock(blockPos.offset(blockPos2.getX() / 2, blockPos2.getY(), blockPos2.getZ() / 2));
            cir.setReturnValue(emptyBetween && forgeLike.hasEnchantPowerBonus(state, level, blockPos));
        }
    }
}
