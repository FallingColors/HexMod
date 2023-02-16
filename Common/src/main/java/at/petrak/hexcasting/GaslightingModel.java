package at.petrak.hexcasting;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GaslightingModel implements BakedModel {
    private static int GASLIGHTING_AMOUNT = 0;
    private static boolean HAS_RENDERED_THIS_FRAME = false;

    private final List<BakedModel> variants;
    private final BakedModel wrapped;

    public GaslightingModel(List<BakedModel> variants) {
        this.variants = variants;
        this.wrapped = this.variants.get(0);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
        HAS_RENDERED_THIS_FRAME = true;

        var idx = Math.abs(GASLIGHTING_AMOUNT % this.variants.size());
        return this.variants.get(idx).getQuads(state, direction, random);
    }

    public boolean useAmbientOcclusion() {
        return this.wrapped.useAmbientOcclusion();
    }

    public boolean isGui3d() {
        return this.wrapped.isGui3d();
    }

    public boolean usesBlockLight() {
        return this.wrapped.usesBlockLight();
    }

    public boolean isCustomRenderer() {
        return this.wrapped.isCustomRenderer();
    }

    public TextureAtlasSprite getParticleIcon() {
        return this.wrapped.getParticleIcon();
    }

    public ItemTransforms getTransforms() {
        return this.wrapped.getTransforms();
    }

    public ItemOverrides getOverrides() {
        return this.wrapped.getOverrides();
    }

    public static void postFrameCheckRendered() {
        if (!HAS_RENDERED_THIS_FRAME) {
            GASLIGHTING_AMOUNT += 1;
        }
        HAS_RENDERED_THIS_FRAME = false;
    }
}
