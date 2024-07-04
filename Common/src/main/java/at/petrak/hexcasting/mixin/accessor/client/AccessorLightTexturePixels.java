package at.petrak.hexcasting.mixin.accessor.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightTexture.class)
public interface AccessorLightTexturePixels {
    @Accessor("lightPixels")
    public NativeImage getLightPixels();
}
