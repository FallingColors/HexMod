package at.petrak.hexcasting.mixin.accessor.client;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface AccessorGameRenderer {
    @Invoker("loadEffect")
    void hex$loadEffect(ResourceLocation loc);

    @Accessor("postEffect")
    PostChain hex$postEffect();
}
