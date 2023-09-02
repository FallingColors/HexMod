package at.petrak.hexcasting.client.shader;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.mixin.accessor.client.AccessorRenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

// https://github.com/VazkiiMods/Botania/blob/3a43accc2fbc439c9f2f00a698f8f8ad017503db/Common/src/main/java/vazkii/botania/client/core/helper/RenderHelper.java
public final class HexRenderTypes extends RenderType {

	private HexRenderTypes(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
		super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
		throw new UnsupportedOperationException("Should not be instantiated");
	}

	private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
										int bufSize, boolean hasCrumbling, boolean sortOnUpload, RenderType.CompositeState glState) {
		return AccessorRenderType.hex$create(name, format, mode, bufSize, hasCrumbling, sortOnUpload, glState);
	}

	private static final Function<ResourceLocation, RenderType> GRAYSCALE_PROVIDER = Util.memoize(texture -> {
		CompositeState glState = RenderType.CompositeState.builder()
				.setShaderState(new ShaderStateShard(HexShaders::grayscale))
				.setTextureState(new TextureStateShard(texture, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);

		return makeLayer(HexAPI.MOD_ID + ":grayscale", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, glState);
	});

	public static RenderType getGrayscaleLayer(ResourceLocation texture) {
		return GRAYSCALE_PROVIDER.apply(texture);
	}

}
