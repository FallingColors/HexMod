package at.petrak.hexcasting.interop.utils;

import at.petrak.hexcasting.api.spell.math.HexCoord;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.client.RenderLib;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class PatternDrawingUtil {
    public static void drawPattern(PoseStack poseStack, int x, int y, List<PatternEntry> patterns, List<Vec2> dots,
        boolean strokeOrder, long animTicks, int outer, int innerLight, int innerDark, int dotColor) {
        poseStack.pushPose();
        poseStack.translate(x, y, 1);
        var mat = poseStack.last().pose();
        var prevShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
//        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // mark center
//        RenderLib.drawSpot(mat, Vec2.ZERO, 0f, 0f, 0f, 1f);

        for (var pat : patterns) {
            RenderLib.drawLineSeq(mat, pat.zappyPoints(), 5f, 0, outer, outer, null);
            RenderLib.drawLineSeq(mat, pat.zappyPoints(), 2f, 0,
                strokeOrder ? innerDark : innerLight,
                innerLight,
                strokeOrder ? animTicks / 20f : null);

            if (strokeOrder) {
                RenderLib.drawSpot(mat, pat.zappyPoints().get(0), 2.5f, 1f, 0.1f, 0.15f, 0.6f);
            }
        }

        float dotR = FastColor.ARGB32.red(dotColor) / 255f;
        float dotG = FastColor.ARGB32.green(dotColor) / 255f;
        float dotB = FastColor.ARGB32.blue(dotColor) / 255f;
        float dotA = FastColor.ARGB32.alpha(dotColor) / 255f;

        for (var dot : dots) {
            RenderLib.drawSpot(mat, dot, 1.5f, dotR, dotG, dotB, dotA);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> prevShader);

        RenderSystem.enableCull();

        poseStack.popPose();
    }

    public static PatternRenderingData loadPatterns(List<Pair<HexPattern, HexCoord>> patterns) {
        var patternEntries = new ArrayList<PatternEntry>(patterns.size());

        var fakeScale = 1;
        var seenFakePoints = new ArrayList<Vec2>();
        var seenCoords = new HashSet<HexCoord>();
        for (var pair : patterns) {
            var pattern = pair.getFirst();
            var origin = pair.getSecond();
            for (var pos : pattern.positions(origin)) {
                var px = HexUtils.coordToPx(pos, fakeScale, Vec2.ZERO);
                seenFakePoints.add(px);
            }

            // And while we're looping add the (COORD ONLY) things internally
            patternEntries.add(new PatternEntry(pattern, origin, new ArrayList<>()));
            seenCoords.addAll(pattern.positions(origin));
        }
        var fakeCom = HexUtils.findCenter(seenFakePoints);

        var maxDx = -1f;
        var maxDy = -1f;
        for (var dot : seenFakePoints) {
            var dx = Mth.abs(dot.x - fakeCom.x);
            if (dx > maxDx) {
                maxDx = dx;
            }
            var dy = Mth.abs(dot.y - fakeCom.y);
            if (dy > maxDy) {
                maxDy = dy;
            }
        }
        var hexSize = Math.min(12, Math.min(120 / 2.5f / maxDx, 70 / 2.5f / maxDy));

        var seenRealPoints = new ArrayList<Vec2>();
        for (var pat : patternEntries) {
            for (var pos : pat.pattern().positions(pat.origin())) {
                var px = HexUtils.coordToPx(pos, hexSize, Vec2.ZERO);
                seenRealPoints.add(px);
            }
        }
        var realCom = HexUtils.findCenter(seenRealPoints);

        // and NOW for real!
        for (var pat : patternEntries) {
            var localOrigin = HexUtils.coordToPx(pat.origin(), hexSize, realCom.negated());
            var points = pat.pattern().toLines(hexSize, localOrigin);
            pat.zappyPoints().addAll(RenderLib.makeZappy(points, 10f, 0.8f, 0f, 0f));
        }

        var pathfinderDots = seenCoords.stream()
            .map(coord -> HexUtils.coordToPx(coord, hexSize, realCom.negated())).toList();

        return new PatternRenderingData(patternEntries, pathfinderDots, hexSize);
    }

    public record PatternRenderingData(List<PatternEntry> patterns, List<Vec2> pathfinderDots, float hexSize) {
        // NO-OP
    }
}
