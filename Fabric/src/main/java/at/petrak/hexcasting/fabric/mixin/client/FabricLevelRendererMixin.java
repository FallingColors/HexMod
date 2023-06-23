package at.petrak.hexcasting.fabric.mixin.client;

import at.petrak.hexcasting.fabric.xplat.FabricClientXplatImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelRenderer.class)
public class FabricLevelRendererMixin {
    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;levelFogColor()V",
            ordinal = 0),
        locals = LocalCapture.CAPTURE_FAILSOFT)
    private void snagFrustumFromLevelRenderer(PoseStack poseStack, float f, long arg2, boolean bl, Camera camera,
          GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f matrix4f, CallbackInfo ci,
          ProfilerFiller profilerFiller, boolean bl2, Vec3 vec3, double d, double e, double g, Matrix4f matrix4f2,
          boolean bl3, Frustum frustum) {
        FabricClientXplatImpl.LEVEL_RENDERER_FRUSTUM = frustum;
    }
}
