package at.petrak.hexcasting.mixin.accessor.client;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RenderType.CompositeRenderType.class, remap = false)
public interface AccessorCompositeRenderType {
    @Invoker("state")
    RenderType.CompositeState hex$state();
}
