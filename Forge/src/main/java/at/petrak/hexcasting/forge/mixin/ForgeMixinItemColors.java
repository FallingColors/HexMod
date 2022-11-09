package at.petrak.hexcasting.forge.mixin;

import at.petrak.hexcasting.forge.ForgeHexClientInitializer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemColors.class)
public class ForgeMixinItemColors {
    @Inject(method = "createDefault", at = @At("RETURN"))
    private static void hex$onCreateDefault(BlockColors blockColors, CallbackInfoReturnable<ItemColors> info) {
        ForgeHexClientInitializer.GLOBAL_ITEM_COLORS = info.getReturnValue();
    }
}
