package at.petrak.hexcasting.client.render;


import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.mixin.accessor.client.AccessorLightTexturePixels;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class PatternRenderer {

    public static void renderPattern(HexPattern pattern, PoseStack ps, PatternRenderSettings patSets, PatternColors patColors, double seed) {
        renderPattern(pattern, ps, null, patSets, patColors, seed, null, new Vec3(0,0,1));
    }

    public static void renderPattern(HexPattern pattern, PoseStack ps, @Nullable MultiBufferSource provider, PatternRenderSettings patSets, PatternColors patColors, double seed, Integer light, Vec3 normalVec){
        if(patSets.speed == 0 && PatternTextureManager.useTextures){
            // try doing texture rendering
            boolean didRender = renderPatternTexture(pattern, ps, provider, patSets, patColors, seed, (light != null) ? light : LightTexture.FULL_BRIGHT, normalVec);
            if(didRender) return;
        }

        var oldShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        ps.pushPose();

        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(pattern, patSets, seed);

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

        List<Vec2> zappyRenderSpace = new ArrayList<>();

        for (Vec2 point : zappyPattern) {
            zappyRenderSpace.add(new Vec2(
                (float) (((point.x - staticPoints.minX) * staticPoints.finalScale) + staticPoints.offsetX),
                (float) (((point.y - staticPoints.minY) * staticPoints.finalScale) + staticPoints.offsetY)
            ));
        }


        // TODO: tweak this to suck less or rewrite drawLineSeq to support light -- they're yellow?? in dark lighting??
        if(light != null){
            ClientLevel cLevel = Minecraft.getInstance().level;
//            float blockBrightness = LightTexture.getBrightness(cLevel.dimensionType(), LightTexture.block(light));
//            float skyBlockBrightness = LightTexture.getBrightness(cLevel.dimensionType(), LightTexture.sky(light));
//            float skyBrightness = cLevel.getSkyDarken(Minecraft.getInstance().getFrameTime());

//            float brightness = (float)Math.min(1.0,((skyBrightness * skyBlockBrightness) + blockBrightness) / 1.5);
            // get brightness straight from the texture.
            int bri = ((AccessorLightTexturePixels)Minecraft.getInstance().gameRenderer.lightTexture()).getLightPixels().getPixelRGBA(
                    LightTexture.block(light), LightTexture.sky(light));
            RenderSystem.setShaderColor(
                    FastColor.ARGB32.red(bri)/255f,
                    FastColor.ARGB32.red(bri)/255f,
                    FastColor.ARGB32.red(bri)/255f,
                1f);
        }


        RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, patSets.outerWidthProvider.apply((float)(staticPoints.finalScale)), 0.005f, patColors.outerEndColor, patColors.outerStartColor);
        RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, patSets.innerWidthProvider.apply((float)(staticPoints.finalScale)), 0f, patColors.innerEndColor, patColors.innerStartColor);
        // TODO: probably want to have option to render little dots and stuff.
        // 2D -- supports gradient stroke (goes through tessellator)
//        if(vc == null){
//        } else { // 3D -- goes through vc, supports lighting -- kinda
//            ps.mulPoseMatrix(new Matrix4f().scaling(-1.0f, 1.0f, 1.0f));
//            ps.pushPose();
//            ps.translate(0, 0, 0.005f);
//            TheCoolerRenderLib.theCoolerDrawLineSeq(ps.last().pose(), ps.last().normal(), light, vc, zappyRenderSpace, patSets.outerWidthProvider.apply((float)(scale * baseScale)), patColors.outerStartColor);
//            ps.popPose();
//            TheCoolerRenderLib.theCoolerDrawLineSeq(ps.last().pose(), ps.last().normal(), light, vc, zappyRenderSpace, patSets.innerWidthProvider.apply((float)(scale * baseScale)), patColors.innerStartColor);
//        }

        ps.popPose();
//        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShader(() -> oldShader);
    }

    private static boolean renderPatternTexture(HexPattern pattern, PoseStack ps, @Nullable MultiBufferSource provider, PatternRenderSettings patSets, PatternColors patColors, double seed, int light, Vec3 normalVec){
        Optional<Map<String, ResourceLocation>> maybeTextures = PatternTextureManager.getTextures(pattern, patSets, seed);
        if(maybeTextures.isEmpty()){
            return false;
        }
        ShaderInstance oldShader = RenderSystem.getShader();

        Map<String, ResourceLocation> textures = maybeTextures.get();


        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(pattern, patSets, seed);

        RenderSystem.enableDepthTest();

        VertexConsumer vc = setupVC(provider, textures.get("outer"));

        textureVertex(vc, ps, 0, 0, 0.0005f, 0, 0, normalVec, light, patColors.outerStartColor);
        textureVertex(vc, ps, 0, (float)staticPoints.fullHeight, 0.0005f, 0, 1, normalVec, light, patColors.outerStartColor);
        textureVertex(vc, ps, (float)staticPoints.fullWidth, (float)staticPoints.fullHeight, 0, 1, 1, normalVec, light, patColors.outerStartColor);
        textureVertex(vc, ps, (float)staticPoints.fullWidth, 0, 0.0005f, 1, 0, normalVec, light, patColors.outerStartColor);

        endDraw(provider, textures.get("outer"), vc);
        vc = setupVC(provider, textures.get("inner"));

        textureVertex(vc, ps, 0, 0, 0.001f, 0, 0, normalVec, light, patColors.innerStartColor);
        textureVertex(vc, ps, 0, (float)staticPoints.fullHeight, 0.001f, 0, 1, normalVec, light, patColors.innerStartColor);
        textureVertex(vc, ps, (float)staticPoints.fullWidth, (float)staticPoints.fullHeight, 0.001f, 1, 1, normalVec, light, patColors.innerStartColor);
        textureVertex(vc, ps, (float)staticPoints.fullWidth, 0, 0.001f, 1, 0, normalVec, light, patColors.innerStartColor);


        endDraw(provider, textures.get("inner"), vc);

        RenderSystem.setShader(() -> oldShader);

        return true;
    }

    private static VertexConsumer setupVC(@Nullable MultiBufferSource provider, ResourceLocation texture){
        VertexConsumer vc;

        RenderType layer = RenderType.entityTranslucentCull(texture);

        if(provider == null){
            Tesselator.getInstance().getBuilder().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
            vc = Tesselator.getInstance().getBuilder();
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(GameRenderer::getRendertypeEntityTranslucentCullShader);
        } else {
            vc = provider.getBuffer(layer);
        }
        layer.setupRenderState();
        return vc;
    }

    private static void endDraw(@Nullable MultiBufferSource provider, ResourceLocation texture, VertexConsumer vc){
        if(provider == null){
            RenderType layer = RenderType.entityTranslucentCull(texture);
            layer.end(Tesselator.getInstance().getBuilder(), VertexSorting.ORTHOGRAPHIC_Z);
        }
    }

    private static void textureVertex(VertexConsumer vc, PoseStack ps, float x, float y, float z, float u, float v, Vec3 normals, int light, int color){
        vc.vertex(ps.last().pose(), x, y, 0)
            .color(color)
            .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
            .normal(ps.last().normal(), (float)normals.x, (float)normals.y, (float)normals.z)
            .endVertex();
    }
}
