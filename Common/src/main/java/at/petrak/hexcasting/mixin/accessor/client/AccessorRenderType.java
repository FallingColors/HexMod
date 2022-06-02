package at.petrak.hexcasting.mixin.accessor.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// https://github.com/VazkiiMods/Botania/blob/13b7bcd9cbb6b1a418b0afe455662d29b46f1a7f/Xplat/src/main/java/vazkii/botania/mixin/client/AccessorRenderType.java
@Mixin(RenderType.class)
public interface AccessorRenderType {
	@Invoker("create")
	static RenderType.CompositeRenderType hex$create(String string, VertexFormat vertexFormat,
												 VertexFormat.Mode mode, int bufSize, boolean hasCrumbling, boolean sortOnUpload,
												 RenderType.CompositeState compositeState) {
		throw new IllegalStateException();
	}
}
