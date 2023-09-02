package at.petrak.hexcasting.forge.mixin;

import at.petrak.hexcasting.forge.ForgeHexClientInitializer;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public class ForgeMixinBlockColors {
    @Inject(method = "createDefault", at = @At("RETURN"))
    private static void hex$onCreateDefault(CallbackInfoReturnable<BlockColors> info) {
        ForgeHexClientInitializer.GLOBAL_BLOCK_COLORS = info.getReturnValue();
    }
}
