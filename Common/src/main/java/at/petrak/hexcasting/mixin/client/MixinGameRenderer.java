package at.petrak.hexcasting.mixin.client;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @WrapOperation(
        method = "processBlurEffect",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;setUniform(Ljava/lang/String;F)V")
    )
    private void scaleBlurWhenPanningGrid(PostChain instance, String string, float f, Operation<Void> original) {
        GameRenderer renderer = (GameRenderer)(Object)this;
        Screen screen = renderer.getMinecraft().screen;
        if (screen instanceof GuiSpellcasting grid) {
            original.call(instance, string, grid.getPanDistance() / 10);
        } else {
            original.call(instance, string, f);
        }
    }
}
