package at.petrak.hexcasting.client.render;


import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.mixin.accessor.client.AccessorLightTexturePixels;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PatternRenderer {

    public static void renderPattern(HexPattern pattern, PoseStack ps, PatternRenderSettings patSets, PatternColors patColors, double seed) {
        renderPattern(pattern, ps, null, patSets, patColors, seed, null);
    }

    public static void renderPattern(HexPattern pattern, PoseStack ps, @Nullable VertexConsumer vc, PatternRenderSettings patSets, PatternColors patColors, double seed, Integer light){
        if(patSets.speed == 0){
            // try doing texture rendering
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

        int patStepsX = (int)Math.round(staticPoints.rangeX / 1.5);
        int patStepsY = (int)Math.round(staticPoints.rangeY / 1.7);

        // scales the patterns so that each point is patSets.baseScale units apart
        double baseScale = patSets.baseScale / 1.5;

        // size of the pattern in pose space with no other adjustments
        double baseWidth = staticPoints.rangeX * baseScale;
        double baseHeight = staticPoints.rangeY * baseScale;

        // make sure that the scale fits within our min sizes
        double scale = Math.max(1.0, Math.max(patSets.minWidth / baseWidth, patSets.minHeight / baseHeight));


        // scale down if needed to fit in vertical space
        if(patSets.fitAxis.vertFit){
            scale = Math.min(scale, (patSets.spaceHeight - 2 * patSets.vPadding)/(baseHeight));
        }

        // scale down if needed to fit in horizontal space
        if(patSets.fitAxis.horFit){
            scale = Math.min(scale, (patSets.spaceWidth - 2 * patSets.hPadding)/(baseWidth));
        }


        // either the space given or however long it goes if it's not fitted.
        double fullWidth = (baseWidth * scale) + 2 * patSets.hPadding;
        double fullHeight = (baseHeight * scale) + 2 * patSets.vPadding;

        if(patSets.fitAxis.horFit) fullWidth = Math.max(patSets.spaceWidth, fullWidth);
        if(patSets.fitAxis.vertFit) fullHeight = Math.max(patSets.spaceHeight, fullHeight);

        double offsetX = (fullWidth - baseWidth * scale) / 2;
        double offsetY = (fullHeight - baseHeight * scale) / 2;

        List<Vec2> zappyRenderSpace = new ArrayList<>();

        for (Vec2 point : zappyPattern) {
            zappyRenderSpace.add(new Vec2(
                    (float) (((point.x - staticPoints.minX) * baseScale * scale) + offsetX),
                    (float) (((point.y - staticPoints.minY) * baseScale * scale) + offsetY)
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


        RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, patSets.outerWidthProvider.apply((float)(scale * baseScale)), 0.005f, patColors.outerEndColor, patColors.outerStartColor);
        RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, patSets.innerWidthProvider.apply((float)(scale * baseScale)), 0f, patColors.innerEndColor, patColors.innerStartColor);
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
}
