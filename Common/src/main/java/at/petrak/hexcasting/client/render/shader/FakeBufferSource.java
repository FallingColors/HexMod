package at.petrak.hexcasting.client.render.shader;

import at.petrak.hexcasting.mixin.accessor.client.AccessorCompositeRenderType;
import at.petrak.hexcasting.mixin.accessor.client.AccessorEmptyTextureStateShard;
import at.petrak.hexcasting.mixin.accessor.client.AccessorRenderStateShard;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Function;

public record FakeBufferSource(MultiBufferSource parent,
                               Function<ResourceLocation, RenderType> mapper) implements MultiBufferSource {

    private static final MethodHandle TEXTURE_STATE_GETTER = getTextureStateGetter();

    private static MethodHandle getTextureStateGetter() {
        try {
            Field f = RenderType.CompositeState.class.getDeclaredField("textureState");
            f.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(f);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
        if (((AccessorRenderStateShard) renderType).hex$name().equals("entity_cutout_no_cull")
            && renderType instanceof RenderType.CompositeRenderType) {
            RenderType.CompositeState state = ((AccessorCompositeRenderType) renderType).hex$state();
            RenderStateShard.EmptyTextureStateShard shard = TEXTURE_STATE_GETTER != null
                ? getTextureState(state)
                : null;
            if (shard != null) {
                Optional<ResourceLocation> texture = ((AccessorEmptyTextureStateShard) shard).hex$cutoutTexture();
                if (texture.isPresent()) {
                    return parent.getBuffer(mapper.apply(texture.get()));
                }
            }
        }
        return parent.getBuffer(renderType);
    }

    @SuppressWarnings("unchecked")
    private static RenderStateShard.EmptyTextureStateShard getTextureState(RenderType.CompositeState state) {
        try {
            return (RenderStateShard.EmptyTextureStateShard) TEXTURE_STATE_GETTER.invoke(state);
        } catch (Throwable e) {
            return null;
        }
    }
}
