package at.petrak.hexcasting.client.render;


import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PatternRenderer {

    public static void renderPattern(HexPattern pattern, PoseStack ps, PatternRenderSettings patSets, double seed){
        var oldShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        ps.pushPose();

        // Resolution is the number of sub-voxels in the block for rendering purposes, 16 is the default
        // padding is the space to leave on the edges free of pattern
//        int resolution = 16;
//        int padding = resolution * PatternTextureManager.paddingByBlockSize / PatternTextureManager.resolutionByBlockSize;

//        // and now Z is out?
//        ps.translate(0, 0, -0.5);
//        ps.scale(1f / resolution, 1f / resolution, 1f / resolution);
//        ps.translate(0, 0, 1.01);

        List<Vec2> lines1 = pattern.toLines(1, Vec2.ZERO);
        Set<Integer> dupIndices = RenderLib.findDupIndices(pattern.positions());
        List<Vec2> zappyPattern = RenderLib.makeZappy(lines1, dupIndices,
                patSets.hops, patSets.variance, patSets.speed, patSets.flowIrregular, patSets.readabilityOffset, patSets.lastSegmentLenProportion, seed);

        // always do space calculations with the static version of the pattern
        // so that it doesn't jump around resizing itself.
        List<Vec2> zappyPatternSpace = RenderLib.makeZappy(lines1, dupIndices,
                patSets.hops, patSets.variance, 0f, patSets.flowIrregular, patSets.readabilityOffset, patSets.lastSegmentLenProportion, seed);

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (Vec2 point : zappyPatternSpace)
        {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        int patStepsX = (int)Math.round(rangeX / 1.5);
        int patStepsY = (int)Math.round(rangeY / 1.7);

        /*
        <ne, qaq> -- 2 up 1 across: rangeX: 1.7320507764816284 rangeY: 3.0
        <w, ww> -- flat, 3 across: rangeX: 5.196152210235596 rangeY: 0.06766534224152565
        <w, aa> -- single triangle: rangeX: 1.7320507764816284 rangeY: 1.5277782939374447
         */
//        HexAPI.LOGGER.info("rangeX: " + rangeX + " rangeY: " + rangeY);

        // scales the patterns so that each point is patSets.baseScale units apart
        double baseScale = patSets.baseScale / 1.5;

        // size of the pattern in pose space with no other adjustments
        double baseWidth = rangeX * baseScale;
        double baseHeight = rangeY * baseScale;

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

//
//        double offsetX = ((- 2 * patSets.hPadding) - baseWidth * scale) / 2;
//        double offsetY = ((- 2 * patSets.vPadding) - baseHeight * scale) / 2;

        List<Vec2> zappyRenderSpace = new ArrayList<>();

        for (Vec2 point : zappyPattern) {
            zappyRenderSpace.add(new Vec2(
                    (float) (((point.x - minX) * baseScale * scale) + patSets.hPadding),
                    (float) (((point.y - minY) * baseScale * scale) + patSets.vPadding)
            ));
        }

        RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, 1f, 0f, patSets.getOuterEndColor(), patSets.getOuterStartColor());
        RenderLib.drawLineSeq(ps.last().pose(), zappyRenderSpace, 1f, 0.01f, patSets.getInnerEndColor(), patSets.getInnerStartColor());

        ps.popPose();
        RenderSystem.enableCull();
        RenderSystem.setShader(() -> oldShader);
    }

    // TODO: make this not be duplicate code / figure out how to integrate it with render function / maybe cache some bits
    public static double getPatternWHRatio(HexPattern pattern, PatternRenderSettings patSets, double seed){
        List<Vec2> lines1 = pattern.toLines(1, Vec2.ZERO);
        Set<Integer> dupIndices = RenderLib.findDupIndices(pattern.positions());
        List<Vec2> zappyPattern = RenderLib.makeZappy(lines1, dupIndices,
                patSets.hops, patSets.variance, patSets.speed, patSets.flowIrregular, patSets.readabilityOffset, patSets.lastSegmentLenProportion, seed);

        // always do space calculations with the static version of the pattern
        // so that it doesn't jump around resizing itself.
        List<Vec2> zappyPatternSpace = RenderLib.makeZappy(lines1, dupIndices,
                patSets.hops, patSets.variance, 0f, patSets.flowIrregular, patSets.readabilityOffset, patSets.lastSegmentLenProportion, seed);

        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        for (Vec2 point : zappyPatternSpace)
        {
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
        }

        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        return rangeX/rangeY;
    }
}
