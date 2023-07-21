package at.petrak.hexcasting.mixin.client;

import at.petrak.hexcasting.client.render.overlays.EigengrauOverlay;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PostChain.class)
public class MixinPostChain {
    @Inject(method = "getRenderTarget", at = @At("RETURN"), cancellable = true)
    void hex$useOurTargets(String target, CallbackInfoReturnable<RenderTarget> cir) {
        if (EigengrauOverlay.SPECIAL_BZ_SIM_ID.toString().equals(target)) {
            cir.setReturnValue(EigengrauOverlay.EIGENGRAU_BZ_SIMULATION);
        } else if (EigengrauOverlay.SPECIAL_BZ_VEIL_ID.toString().equals(target)) {
            cir.setReturnValue(EigengrauOverlay.EIGENGRAU_VEIL);
        }
    }
}
