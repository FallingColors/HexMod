package at.petrak.hexcasting.fabric.mixin.client;

import at.petrak.hexcasting.fabric.event.MouseScrollCallback;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public class FabricMouseHandlerMixin {
    @Inject(method = "onScroll", cancellable = true, locals = LocalCapture.PRINT, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
    private void onScroll(CallbackInfo ci, double delta) {
        var cancel = MouseScrollCallback.EVENT.invoker().interact(delta);
        if (cancel) {
            ci.cancel();
        }
    }
}
