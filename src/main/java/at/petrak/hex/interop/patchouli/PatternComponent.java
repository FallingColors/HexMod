package at.petrak.hex.interop.patchouli;

import at.petrak.hex.client.RenderLib;
import at.petrak.hex.hexmath.HexDir;
import at.petrak.hex.hexmath.HexPattern;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec2;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Page that has a hex pattern on it
 */
public class PatternComponent implements ICustomComponent {
    public String signature;
    public String startdir;

    protected transient HexPattern pattern;
    protected transient List<Vec2> straightPoints;
    protected transient int x, y;

    private static final float RADIUS = 20f;

    /**
     * Pass -1, -1 to center it.
     */
    @Override
    public void build(int x, int y, int pagenum) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(PoseStack poseStack, IComponentRenderContext ctx, float partialTicks, int mouseX, int mouseY) {
        poseStack.pushPose();
        poseStack.translate(this.x, this.y, 0);
        RenderLib.drawPattern(poseStack.last().pose(), this.straightPoints, 230, 230, 230, 200);

        // just try to render anything at all oh my god please
        RenderLib.drawLineSeq(poseStack.last().pose(), Arrays.asList(Vec2.ZERO, new Vec2(116, 156)), 2f, 1, 255, 255,
                255,
                255);
        poseStack.popPose();
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        var dirstr = lookup.apply(IVariable.wrap(this.startdir)).asString("EAST");
        var dir = HexDir.valueOf(dirstr.toUpperCase());
        var sig = lookup.apply(IVariable.wrap(this.signature)).asString("");
        this.pattern = HexPattern.FromAnglesSig(sig, dir);

        var com = this.pattern.getCenter(RADIUS);
        this.straightPoints = RenderLib.hexPatternToLines(this.pattern, RADIUS, com);
    }
}
