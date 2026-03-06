package at.petrak.hexcasting.mixin.accessor.client;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RenderStateShard.class, remap = false)
public interface AccessorRenderStateShard {
    @Accessor("name")
    String hex$name();
}
