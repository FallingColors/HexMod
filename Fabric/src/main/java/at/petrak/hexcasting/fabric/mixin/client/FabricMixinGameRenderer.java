package at.petrak.hexcasting.fabric.mixin.client;

import at.petrak.hexcasting.client.shader.HexShaders;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

// https://github.com/VazkiiMods/Botania/blob/b1f1cf80e10b1c739b0188171b367d9cefc4d3c7/Fabric/src/main/java/vazkii/botania/fabric/mixin/client/FabricMixinGameRenderer.java
@Mixin(GameRenderer.class)
public class FabricMixinGameRenderer {
	@SuppressWarnings("InvalidInjectorMethodSignature")
	@Inject(
			method = "reloadShaders",
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lcom/google/common/collect/Lists;newArrayListWithCapacity(I)Ljava/util/ArrayList;",
					remap = false
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void loadShaders(ResourceManager resourceManager, CallbackInfo ci,
							 List<Program> _programsToClose,
							 List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shadersToLoad)
			throws IOException {
		HexShaders.init(resourceManager, shadersToLoad::add);
	}
}
