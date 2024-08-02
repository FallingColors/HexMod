package at.petrak.hexcasting.client.render;


import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PatternRenderer {

    public static void renderPattern(HexPattern pattern, PoseStack ps, PatternSettings patSets, PatternColors patColors, double seed, int resPerUnit) {
        renderPattern(pattern, ps, null, patSets, patColors, seed, resPerUnit);
    }

    public static void renderPattern(HexPattern pattern, PoseStack ps, @Nullable WorldlyBits worldlyBits, PatternSettings patSets, PatternColors patColors, double seed, int resPerUnit) {
        renderPattern(HexPatternLike.of(pattern), ps, worldlyBits, patSets, patColors, seed, resPerUnit);
    }

    /**
     * Renders a pattern (or rather a pattern-like) according to the given settings.
     * @param patternlike the pattern (or more generally the lines) to render.
     * @param ps pose/matrix stack to render based on. (0,0) is treated as the top left corner. The size of the render is determined by patSets.
     * @param worldlyBits used for rendering with light/normals/render-layers if possible. This is optional and probably shouldn't be used for UI rendering.
     * @param patSets settings that control how the pattern is drawn.
     * @param patColors colors to use for drawing the pattern and dots.
     * @param seed seed to use for zappy wobbles.
     * @param resPerUnit the texture resolution per pose unit space to be used *if* the texture renderer is used.
     */
    public static void renderPattern(HexPatternLike patternlike, PoseStack ps, @Nullable WorldlyBits worldlyBits, PatternSettings patSets, PatternColors patColors, double seed, int resPerUnit){
        var oldShader = RenderSystem.getShader();
        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(patternlike, patSets, seed);

        boolean shouldRenderDynamic = true;

        // only do texture rendering if it's static and has solid colors
        if(patSets.getSpeed() == 0 && PatternTextureManager.useTextures && patColors.innerStartColor() == patColors.innerEndColor()
        && patColors.outerStartColor() == patColors.outerEndColor()){
            boolean didRender = renderPatternTexture(patternlike, ps, worldlyBits, patSets, patColors, seed, resPerUnit);
            if(didRender) shouldRenderDynamic = false;
        }
        if(shouldRenderDynamic){
            List<Vec2> zappyPattern;

            if(patSets.getSpeed() == 0) {
                // re-use our static points if we're rendering a static pattern anyway
                zappyPattern = staticPoints.zappyPoints;
            } else {
                List<Vec2> nonzappyLines = patternlike.getNonZappyPoints();
                Set<Integer> dupIndices = RenderLib.findDupIndices(nonzappyLines);
                zappyPattern = RenderLib.makeZappy(nonzappyLines, dupIndices,
                        patSets.getHops(), patSets.getVariance(), patSets.getSpeed(), patSets.getFlowIrregular(),
                        patSets.getReadabilityOffset(), patSets.getLastSegmentProp(), seed);
            }

            List<Vec2> zappyRenderSpace = staticPoints.scaleVecs(zappyPattern);

            if(FastColor.ARGB32.alpha(patColors.outerEndColor()) != 0 && FastColor.ARGB32.alpha(patColors.outerStartColor()) != 0){
                RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, (float)patSets.getOuterWidth(staticPoints.finalScale),
                        patColors.outerStartColor(), patColors.outerEndColor(), VCDrawHelper.getHelper(worldlyBits, ps,outerZ));
            }
            if(FastColor.ARGB32.alpha(patColors.innerEndColor()) != 0 && FastColor.ARGB32.alpha(patColors.innerStartColor()) != 0) {
                RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, (float)patSets.getInnerWidth(staticPoints.finalScale),
                        patColors.innerStartColor(), patColors.innerEndColor(), VCDrawHelper.getHelper(worldlyBits, ps,innerZ));
            }
        }

        // render dots and grid dynamically

        float dotZ = 0.0011f;

        if(FastColor.ARGB32.alpha(patColors.startingDotColor()) != 0) {
            RenderLib.drawSpot(ps.last().pose(), staticPoints.dotsScaled.get(0), (float)patSets.getStartDotRadius(staticPoints.finalScale),
                    patColors.startingDotColor(), VCDrawHelper.getHelper(worldlyBits, ps, dotZ));
        }

        if(FastColor.ARGB32.alpha(patColors.gridDotsColor()) != 0) {
            for(int i = 1; i < staticPoints.dotsScaled.size(); i++){
                Vec2 gridDot = staticPoints.dotsScaled.get(i);
                RenderLib.drawSpot(ps.last().pose(), gridDot, (float)patSets.getGridDotsRadius(staticPoints.finalScale),
                    patColors.gridDotsColor(), VCDrawHelper.getHelper(worldlyBits, ps, dotZ));
            }
        }

        RenderSystem.setShader(() -> oldShader);
    }

    private static final float outerZ = 0.0005f;
    private static final float innerZ = 0.001f;

    private static boolean renderPatternTexture(HexPatternLike patternlike, PoseStack ps, @Nullable WorldlyBits worldlyBits, PatternSettings patSets, PatternColors patColors, double seed, int resPerUnit){
        Optional<Map<String, ResourceLocation>> maybeTextures = PatternTextureManager.getTextures(patternlike, patSets, seed, resPerUnit);
        if(maybeTextures.isEmpty()){
            return false;
        }

        Map<String, ResourceLocation> textures = maybeTextures.get();
        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(patternlike, patSets, seed);

        VertexConsumer vc;

        if(FastColor.ARGB32.alpha(patColors.outerStartColor()) != 0) {
            VCDrawHelper vcHelper = VCDrawHelper.getHelper(worldlyBits, ps, outerZ, textures.get("outer"));
            vc = vcHelper.vcSetupAndSupply(VertexFormat.Mode.QUADS);

            int cl = patColors.outerStartColor();

            vcHelper.vertex(vc, cl, new Vec2(0, 0), new Vec2(0, 0), ps.last().pose());
            vcHelper.vertex(vc, cl, new Vec2(0, (float) staticPoints.fullHeight), new Vec2(0, 1), ps.last().pose());
            vcHelper.vertex(vc, cl, new Vec2((float) staticPoints.fullWidth, (float) staticPoints.fullHeight), new Vec2(1, 1), ps.last().pose());
            vcHelper.vertex(vc, cl, new Vec2((float) staticPoints.fullWidth, 0), new Vec2(1, 0), ps.last().pose());

            vcHelper.vcEndDrawer(vc);
        }

        if(FastColor.ARGB32.alpha(patColors.innerStartColor()) != 0) {
            VCDrawHelper vcHelper = VCDrawHelper.getHelper(worldlyBits, ps, innerZ, textures.get("inner"));
            vc = vcHelper.vcSetupAndSupply(VertexFormat.Mode.QUADS);

            int cl = patColors.innerStartColor();

            vcHelper.vertex(vc, cl, new Vec2(0, 0), new Vec2(0, 0), ps.last().pose());
            vcHelper.vertex(vc, cl, new Vec2(0, (float) staticPoints.fullHeight), new Vec2(0, 1), ps.last().pose());
            vcHelper.vertex(vc, cl, new Vec2((float) staticPoints.fullWidth, (float) staticPoints.fullHeight), new Vec2(1, 1), ps.last().pose());
            vcHelper.vertex(vc, cl, new Vec2((float) staticPoints.fullWidth, 0), new Vec2(1, 0), ps.last().pose());

            vcHelper.vcEndDrawer(vc);
        }

        return true;
    }

    // TODO did we want to un-hardcode this for accessibility reasons ?
    public static boolean shouldDoStrokeGradient(){
        return Screen.hasControlDown();
    }

    public record WorldlyBits(@Nullable MultiBufferSource provider, Integer light, Vec3 normal){}
}
