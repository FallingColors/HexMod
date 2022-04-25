package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.api.spell.math.HexCoord;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Page that has a hex pattern on it
 */
abstract public class AbstractPatternComponent implements ICustomComponent {
    protected transient int x, y;
    protected transient float hexSize;

    private transient List<PatternEntryInternal> patterns;
    private transient List<Vec2> pathfinderDots;

    /**
     * Pass -1, -1 to center it.
     */
    @Override
    public void build(int x, int y, int pagenum) {
        this.x = x == -1 ? 116 / 2 : x;
        this.y = y == -1 ? 70 : y;
    }

    abstract List<Pair<HexPattern, HexCoord>> getPatterns(UnaryOperator<IVariable> lookup);

    abstract boolean showStrokeOrder();

    @Override
    public void render(PoseStack poseStack, IComponentRenderContext ctx, float partialTicks, int mouseX, int mouseY) {
        poseStack.pushPose();
        poseStack.translate(this.x, this.y, 1);
        var mat = poseStack.last().pose();
        var prevShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
//        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        // mark center
//        RenderLib.drawSpot(mat, Vec2.ZERO, 0f, 0f, 0f, 1f);

        var strokeOrder = this.showStrokeOrder();
        for (var pat : this.patterns) {
            var outer = 0xff_d2c8c8;
            var innerLight = 0xc8_aba2a2;
            var innerDark = 0xc8_322b33;
            RenderLib.drawLineSeq(mat, pat.zappyPoints, 5f, 0, outer, outer, null);
            RenderLib.drawLineSeq(mat, pat.zappyPoints, 2f, 0,
                strokeOrder ? innerDark : innerLight,
                innerLight,
                strokeOrder ? ctx.getTicksInBook() / 20f : null);

            if (strokeOrder) {
                RenderLib.drawSpot(mat, pat.zappyPoints.get(0), 2.5f, 1f, 0.1f, 0.15f, 0.6f);
            }
        }

        for (var dot : this.pathfinderDots) {
            RenderLib.drawSpot(mat, dot, 1.5f, 0.82f, 0.8f, 0.8f, 0.5f);
        }

        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> prevShader);

        poseStack.popPose();
    }


    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        var patterns = this.getPatterns(lookup);
        this.patterns = new ArrayList<>(patterns.size());

        // Center the whole thing so the center of all pieces is in the center.
        // As per PatternTooltipGreeble, we start with a random scale, then re-scale it once we know the COM.
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
            this.patterns.add(new PatternEntryInternal(pattern, origin, new ArrayList<>()));
            seenCoords.addAll(pattern.positions(origin));
        }
        var fakeCom = HexUtils.FindCenter(seenFakePoints);

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
        this.hexSize = Math.min(12, Math.min(120 / 2.5f / maxDx, 70 / 2.5f / maxDy));

        var seenRealPoints = new ArrayList<Vec2>();
        for (var pat : this.patterns) {
            for (var pos : pat.pattern.positions(pat.origin)) {
                var px = HexUtils.coordToPx(pos, this.hexSize, Vec2.ZERO);
                seenRealPoints.add(px);
            }
        }
        var realCom = HexUtils.FindCenter(seenRealPoints);

        // and NOW for real!
        for (var pat : this.patterns) {
            var localOrigin = HexUtils.coordToPx(pat.origin, this.hexSize, realCom.negated());
            var points = pat.pattern.toLines(this.hexSize, localOrigin);
            pat.zappyPoints.addAll(RenderLib.makeZappy(points, 10f, 0.8f, 0f));
        }

        this.pathfinderDots = seenCoords.stream()
            .map(coord -> HexUtils.coordToPx(coord, this.hexSize, realCom.negated()))
            .collect(Collectors.toList());
    }

    private record PatternEntryInternal(HexPattern pattern, HexCoord origin, List<Vec2> zappyPoints) {
    }

    protected static class RawPattern {
        String startdir;
        String signature;
        int q, r;

        RawPattern() {
        }
    }
}
