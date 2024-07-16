package at.petrak.hexcasting.client.render;


import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class PatternRenderer {

    public static void renderPattern(HexPattern pattern, PoseStack ps, PatternRenderSettings patSets, PatternColors patColors, double seed, int resPerUnit) {
        renderPattern(pattern, ps, null, patSets, patColors, seed, null, null, resPerUnit);
    }

    public static void renderPattern(HexPattern pattern, PoseStack ps, @Nullable MultiBufferSource provider, PatternRenderSettings patSets, PatternColors patColors, double seed, Integer light, Vec3 normalVec, int resPerUnit){
        var oldShader = RenderSystem.getShader();
        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(pattern, patSets, seed);

        boolean shouldRenderDynamic = true;

        // only do texture rendering if it's static and has solid colors
        if(patSets.speed == 0 && PatternTextureManager.useTextures && patColors.innerStartColor == patColors.innerEndColor
        && patColors.outerStartColor == patColors.outerEndColor){
            boolean didRender = renderPatternTexture(pattern, ps, provider, patSets, patColors, seed, light == null ? LightTexture.FULL_BRIGHT : light, normalVec, resPerUnit);
            if(didRender) shouldRenderDynamic = false;
        }
        if(shouldRenderDynamic){
            List<Vec2> zappyPattern;

            if(patSets.speed == 0) {
                // re-use our static points if we're rendering a static pattern anyway
                zappyPattern = staticPoints.zappyPoints;
            } else {
                List<Vec2> lines1 = pattern.toLines(1, Vec2.ZERO);
                Set<Integer> dupIndices = RenderLib.findDupIndices(pattern.positions());
                zappyPattern = RenderLib.makeZappy(lines1, dupIndices,
                        patSets.hops, patSets.variance, patSets.speed, patSets.flowIrregular, patSets.readabilityOffset, patSets.lastSegmentLenProportion, seed);
            }

            List<Vec2> zappyRenderSpace = staticPoints.scaleVecs(zappyPattern);

            if(FastColor.ARGB32.alpha(patColors.outerEndColor) != 0 && FastColor.ARGB32.alpha(patColors.outerStartColor) != 0){
                RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, patSets.outerWidthProvider.apply((float)(staticPoints.finalScale)),
                        patColors.outerEndColor, patColors.outerStartColor, new SillyVCHelper(provider, ps, light, normalVec, 0.001f, patColors.outerStartColor));
            }
            if(FastColor.ARGB32.alpha(patColors.innerEndColor) != 0 && FastColor.ARGB32.alpha(patColors.innerStartColor) != 0) {
                RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, patSets.innerWidthProvider.apply((float) (staticPoints.finalScale)),
                        patColors.innerEndColor, patColors.innerStartColor, new SillyVCHelper(provider, ps, light, normalVec, 0.0005f, patColors.innerStartColor));
            }
        }

        // render dots and grid dynamically for now

        if(provider == null) provider.getBuffer(RenderType.solid()); // just to try to refresh it

        float dotZ = 0.0004f;

        if(FastColor.ARGB32.alpha(patColors.startingDotColor) != 0) {
            RenderLib.drawSpot(ps.last().pose(), staticPoints.dotsScaled.get(0), patSets.startingDotRadiusProvider.apply((float) (staticPoints.finalScale)),
                    patColors.startingDotColor, new SillyVCHelper(provider, ps, light, normalVec, dotZ, patColors.innerStartColor));
        }

        if(FastColor.ARGB32.alpha(patColors.gridDotsColor) != 0) {
            for(int i = 1; i < staticPoints.dotsScaled.size(); i++){
                Vec2 gridDot = staticPoints.dotsScaled.get(i);
                RenderLib.drawSpot(ps.last().pose(), gridDot, patSets.gridDotsRadiusProvider.apply((float) (staticPoints.finalScale)),
                    patColors.gridDotsColor, new SillyVCHelper(provider, ps, light, normalVec, dotZ, patColors.innerStartColor));
            }
        }

        RenderSystem.setShader(() -> oldShader);
    }

    private static boolean renderPatternTexture(HexPattern pattern, PoseStack ps, @Nullable MultiBufferSource provider, PatternRenderSettings patSets, PatternColors patColors, double seed, int light, Vec3 normalVec, int resPerUnit){
        Optional<Map<String, ResourceLocation>> maybeTextures = PatternTextureManager.getTextures(pattern, patSets, seed, resPerUnit);
        if(maybeTextures.isEmpty()){
            return false;
        }

        if(normalVec == null) normalVec = new Vec3(1f, 1f, 1f);

        ShaderInstance oldShader = RenderSystem.getShader();

        Map<String, ResourceLocation> textures = maybeTextures.get();

        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(pattern, patSets, seed);

        VertexConsumer vc;

        float outerZ = 0.001f;
        float innerZ = 0.0005f;

        if(FastColor.ARGB32.alpha(patColors.outerStartColor) != 0) {
            vc = setupVC(provider, textures.get("outer"));

            textureVertex(vc, ps, 0, 0, outerZ, 0, 0, normalVec, light, patColors.outerStartColor);
            textureVertex(vc, ps, 0, (float) staticPoints.fullHeight, outerZ, 0, 1, normalVec, light, patColors.outerStartColor);
            textureVertex(vc, ps, (float) staticPoints.fullWidth, (float) staticPoints.fullHeight, outerZ, 1, 1, normalVec, light, patColors.outerStartColor);
            textureVertex(vc, ps, (float) staticPoints.fullWidth, 0, outerZ, 1, 0, normalVec, light, patColors.outerStartColor);

            endDraw(provider, textures.get("outer"), vc);
        }

        if(FastColor.ARGB32.alpha(patColors.innerStartColor) != 0) {
            vc = setupVC(provider, textures.get("inner"));

            textureVertex(vc, ps, 0, 0, innerZ, 0, 0, normalVec, light, patColors.innerStartColor);
            textureVertex(vc, ps, 0, (float) staticPoints.fullHeight, innerZ, 0, 1, normalVec, light, patColors.innerStartColor);
            textureVertex(vc, ps, (float) staticPoints.fullWidth, (float) staticPoints.fullHeight, innerZ, 1, 1, normalVec, light, patColors.innerStartColor);
            textureVertex(vc, ps, (float) staticPoints.fullWidth, 0, innerZ, 1, 0, normalVec, light, patColors.innerStartColor);

            endDraw(provider, textures.get("inner"), vc);
        }
        RenderSystem.setShader(() -> oldShader);

        return true;
    }

    private static VertexConsumer setupVC(@Nullable MultiBufferSource provider, ResourceLocation texture){
        VertexConsumer vc;

        RenderType layer = RenderType.entityTranslucentCull(texture);
        layer.setupRenderState();
        if(provider instanceof MultiBufferSource.BufferSource immediate){
            immediate.endBatch();
        }
        if(provider == null){
            Tesselator.getInstance().getBuilder().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
            vc = Tesselator.getInstance().getBuilder();
            RenderSystem.setShader(GameRenderer::getRendertypeEntityTranslucentCullShader);
        } else {
            vc = provider.getBuffer(layer);
        }
        return vc;
    }

    private static void endDraw(@Nullable MultiBufferSource provider, ResourceLocation texture, VertexConsumer vc){
        if(provider == null){
            RenderType layer = RenderType.entityTranslucentCull(texture);
            layer.end(Tesselator.getInstance().getBuilder(), VertexSorting.ORTHOGRAPHIC_Z);
        }
    }

    private static void textureVertex(VertexConsumer vc, PoseStack ps, float x, float y, float z, float u, float v, Vec3 normals, int light, int color){
        vc.vertex(ps.last().pose(), x, y, z)
            .color(color)
            .uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(ps.last().normal(), (float)normals.x, (float)normals.y, (float)normals.z)
            .endVertex();
    }

    private static class SillyVCHelper implements VCDrawHelper{

        private final MultiBufferSource provider;
        private final Integer light;
        private final Vec3 normVec;
        private final float z;
        private final PoseStack ps;

        private final boolean usesLight;
        private final boolean usesNorm;
        private final int modeIndex;

        private final int alpha;

        private static final RenderType rType = RenderType.solid();

        private static final VertexFormat[] formatsList = {
                DefaultVertexFormat.POSITION_COLOR, // no light no normals
                DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, // yes light no normals
                DefaultVertexFormat.NEW_ENTITY // yes light yes normals
        };

        private static final List<Supplier<ShaderInstance>> shadersList = List.of(
                GameRenderer::getPositionColorShader, // no light no normals
                GameRenderer::getPositionColorLightmapShader, // yes light no normals
                GameRenderer::getRendertypeEntityTranslucentCullShader // yes light yes normals
        );

        // need pose stack for normal matrix
        public SillyVCHelper(@Nullable MultiBufferSource provider, PoseStack ps, Integer light, Vec3 normVec, float z, int color){
            this.provider = provider;
            this.light = light;
            this.normVec = normVec != null ? normVec : new Vec3(1,1,1);
            this.z = z;
            this.ps = ps;

            usesLight = light != null;
            usesNorm = this.normVec != null && ps != null && usesLight; // doesn't really make sense to have norms without lighting?
            modeIndex = (usesLight ? 1 : 0) + (usesNorm ? 1 : 0); // index of formats/shaders to use
            alpha = FastColor.ARGB32.alpha(color);
        }

        @NotNull
        @Override
        public VertexConsumer vcSetupAndSupply(@NotNull VertexFormat.Mode vertMode) {
            // we're not actually using provider here because i don't see a render layer that actually supports this?
            // TODOish: make our own render layer/type for it i guess if we really care, i don't think i do.

            if(provider instanceof MultiBufferSource.BufferSource immediate){
                immediate.endBatch();
            }

            Tesselator.getInstance().getBuilder().begin(vertMode, formatsList[modeIndex]);
            if(usesNorm){
                RenderSystem.setShaderTexture(0, TheCoolerRenderLib.WHITE);
//                rType.setupRenderState();
            }
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            if(usesLight) {
                Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            }
            if(usesNorm){
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                if (Minecraft.useShaderTransparency()) {
                    Minecraft.getInstance().levelRenderer.getTranslucentTarget().bindWrite(false);
                }
            }
            RenderSystem.setShader( shadersList.get(modeIndex));
            return Tesselator.getInstance().getBuilder();
        }

        @Override
        public void vertex(@NotNull VertexConsumer vc, int color, @NotNull Vec2 pos, @NotNull Matrix4f matrix) {

            vc.vertex(matrix, pos.x, pos.y, z)
                .color(color | 0xFF_000000);

            if (usesNorm) vc.uv(pos.x, pos.y); // block format needs a texture, we just set it to plain white
            if (usesNorm) vc.overlayCoords(OverlayTexture.NO_OVERLAY);
            if (usesLight) vc.uv2(light);
            if (usesNorm) vc.normal(ps.last().normal(), (float)normVec.x, (float)normVec.y, (float)normVec.z);

            vc.endVertex();
        }

        @Override
        public void vcEndDrawer(@NotNull VertexConsumer vc) {
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha/255f);
            Tesselator.getInstance().end();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            if(usesLight){
                Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
            }
            if(usesNorm){
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
                if (Minecraft.useShaderTransparency()) {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                }
            }
            RenderSystem.enableCull();
//            if(usesNorm){
//                rType.clearRenderState();
//            }
        }
    }
}
