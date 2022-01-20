package at.petrak.hex.interop.patchouli;

import at.petrak.hex.client.RenderLib;
import at.petrak.hex.hexmath.HexCoord;
import at.petrak.hex.hexmath.HexDir;
import at.petrak.hex.hexmath.HexPattern;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
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
public class PatternComponent implements ICustomComponent {
    @SerializedName("patterns")
    public String patternsRaw;
    @SerializedName("hex_size")
    public String hexSizeRaw;

    protected transient List<PatternEntry> patterns;
    protected transient int x, y;
    protected transient List<Vec2> pathfinderDots;
    protected transient float hexSize;

    /**
     * Pass -1, -1 to center it.
     */
    @Override
    public void build(int x, int y, int pagenum) {
        this.x = x == -1 ? 116 / 2 : x;
        this.y = y == -1 ? 70 : y;
    }

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


        for (var pat : this.patterns) {
            RenderLib.drawLineSeq(mat, pat.zappyPoints, 5f, 0, 210, 200, 200, 255, null);
            RenderLib.drawLineSeq(mat, pat.zappyPoints, 2f, 0, 200, 190, 190, 200, ctx.getTicksInBook() / 20f, 0.5f);

            RenderLib.drawSpot(mat, pat.zappyPoints.get(0), 2.5f, 1f, 0.1f, 0.15f, 0.6f);
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
        this.hexSize = lookup.apply(IVariable.wrap(hexSizeRaw)).asNumber(10f).floatValue();

        var patsRaw = lookup.apply(IVariable.wrap(patternsRaw)).asListOrSingleton();

        // Center the whole thing so the center of all pieces is in the center.
        var comAcc = new Vec2(0, 0);
        var pointsCount = 0;
        this.patterns = new ArrayList<>(patsRaw.size());
        if (patsRaw.isEmpty()) {
            return;
        }
        var seenPoints = new HashSet<HexCoord>();
        for (var ivar : patsRaw) {
            JsonElement json = ivar.unwrap();
            RawPattern raw = new Gson().fromJson(json, RawPattern.class);

            var dir = HexDir.valueOf(raw.startdir);
            var pat = HexPattern.FromAnglesSig(raw.signature, dir);
            var origin = new HexCoord(raw.q, raw.r);
            for (var pos : pat.positions(origin)) {
                comAcc = comAcc.add(RenderLib.coordToPx(pos, this.hexSize, Vec2.ZERO));
                pointsCount++;
            }
            this.patterns.add(new PatternEntry(pat, origin, new ArrayList<>()));
            seenPoints.addAll(pat.positions(origin));
        }

        var comOffset = comAcc.scale(1f / pointsCount).negated();

        for (var pat : this.patterns) {
            var localOrigin = RenderLib.coordToPx(pat.origin, this.hexSize, comOffset);
            var points = pat.pattern.toLines(this.hexSize, localOrigin);
            pat.zappyPoints.addAll(RenderLib.makeZappy(points, 10f, 0.8f, 0f));
        }

        this.pathfinderDots = seenPoints.stream()
                .map(coord -> RenderLib.coordToPx(coord, this.hexSize, comOffset))
                .collect(Collectors.toList());
    }

    private record PatternEntry(HexPattern pattern, HexCoord origin, List<Vec2> zappyPoints) {
    }

    private static class RawPattern {
        String startdir;
        String signature;
        int q, r;

        RawPattern() {
        }
    }
}
