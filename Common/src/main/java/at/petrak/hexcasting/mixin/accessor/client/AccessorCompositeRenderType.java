package at.petrak.hexcasting.mixin.accessor.client;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.CompositeRenderType.class)
public interface AccessorCompositeRenderType {
	@Invoker("state")
	RenderType.CompositeState hex$state();
}
