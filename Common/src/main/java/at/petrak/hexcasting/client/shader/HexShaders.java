package at.petrak.hexcasting.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.function.Consumer;

// https://github.com/VazkiiMods/Botania/blob/3a43accc2fbc439c9f2f00a698f8f8ad017503db/Common/src/main/java/vazkii/botania/client/core/helper/CoreShaders.java
public class HexShaders {
	private static ShaderInstance grayscale;
	public static void init(ResourceManager resourceManager,
							Consumer<Pair<ShaderInstance, Consumer<ShaderInstance>>> registrations) throws IOException {
		registrations.accept(Pair.of(
				new ShaderInstance(resourceManager, "hexcasting__grayscale", DefaultVertexFormat.NEW_ENTITY),
				inst -> grayscale = inst)
		);
	}

	public static ShaderInstance grayscale() {
		return grayscale;
	}
}
