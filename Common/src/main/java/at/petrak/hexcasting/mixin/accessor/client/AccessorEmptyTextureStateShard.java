package at.petrak.hexcasting.mixin.accessor.client;

import java.util.Optional;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderStateShard.EmptyTextureStateShard.class)
public interface AccessorEmptyTextureStateShard {
	@Invoker("cutoutTexture")
	Optional<ResourceLocation> hex$cutoutTexture();
}
