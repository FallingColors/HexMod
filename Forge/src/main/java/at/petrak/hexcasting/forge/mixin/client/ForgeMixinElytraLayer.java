package at.petrak.hexcasting.forge.mixin.client;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElytraLayer.class)
public class ForgeMixinElytraLayer {
    @Inject(
        method = "shouldRender", at = @At("RETURN"),
        cancellable = true,
        remap = false
    )
    private void renderIfAltiora(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player) {
            var altiora = IXplatAbstractions.INSTANCE.getAltiora(player);
            HexAPI.LOGGER.warn(altiora);
            if (altiora != null) {
                cir.setReturnValue(true);
            }
        }
    }
}
