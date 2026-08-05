package at.petrak.hexcasting.fabric.mixin.client;

import at.petrak.hexcasting.fabric.event.MouseScrollCallback;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class FabricMouseHandlerMixin {
    @Inject(method = "onScroll", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"))
    private void onScroll(long winptr, double xOff, double yOff, CallbackInfo ci, @Local(name = "n") int delta) {
        var cancel = MouseScrollCallback.EVENT.invoker().interact(delta);
        if (cancel) {
            ci.cancel();
        }
    }
}
