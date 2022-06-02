package at.petrak.hexcasting.client.shader;

import at.petrak.hexcasting.mixin.accessor.client.AccessorCompositeRenderType;
import at.petrak.hexcasting.mixin.accessor.client.AccessorEmptyTextureStateShard;
import at.petrak.hexcasting.mixin.accessor.client.AccessorRenderStateShard;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public record FakeBufferSource(MultiBufferSource parent, Function<ResourceLocation, RenderType> mapper) implements MultiBufferSource {

	@Override
	@SuppressWarnings("ConstantConditions")
	public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
		if (((AccessorRenderStateShard) renderType).hex$name().equals("entity_cutout_no_cull")
				&& renderType instanceof RenderType.CompositeRenderType) {
			RenderType.CompositeState state = ((AccessorCompositeRenderType) renderType).hex$state();
			RenderStateShard.EmptyTextureStateShard shard = state.textureState;
			Optional<ResourceLocation> texture = ((AccessorEmptyTextureStateShard) shard).hex$cutoutTexture();
			if (texture.isPresent()) {
				return parent.getBuffer(mapper.apply(texture.get()));
			}
		}
		return parent.getBuffer(renderType);
	}
}
