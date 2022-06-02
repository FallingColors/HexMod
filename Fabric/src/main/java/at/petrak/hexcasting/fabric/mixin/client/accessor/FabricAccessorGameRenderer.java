package at.petrak.hexcasting.fabric.mixin.client.accessor;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface FabricAccessorGameRenderer {
	@Invoker("loadEffect")
	void hex$loadEffect(ResourceLocation resourceLocation);
}
