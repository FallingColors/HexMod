package at.petrak.hexcasting.fabric.mixin.client;

import at.petrak.hexcasting.fabric.xplat.FabricClientXplatImpl;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(LevelRenderer.class)
public class FabricLevelRendererMixin {
    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;levelFogColor()V",
            ordinal = 0)
    )
    private void snagFrustumFromLevelRenderer(DeltaTracker deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer,
                                              LightTexture lightTexture, Matrix4f matrix4f, Matrix4f matrix4f2,
                                              CallbackInfo ci, @Local Frustum frustum) {
        FabricClientXplatImpl.LEVEL_RENDERER_FRUSTUM = frustum;
    }
}
