package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.api.spell.math.HexCoord;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.interop.utils.PatternDrawingUtil;
import at.petrak.hexcasting.interop.utils.PatternEntry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.phys.Vec2;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Page that has a hex pattern on it
 */
abstract public class AbstractPatternComponent implements ICustomComponent {
    protected transient int x, y;
    protected transient float hexSize;

    private transient List<PatternEntry> patterns;
    private transient List<Vec2> pathfinderDots;

    /**
     * Pass -1, -1 to center it.
     */
    @Override
    public void build(int x, int y, int pagenum) {
        this.x = x == -1 ? 116 / 2 : x;
        this.y = y == -1 ? 70 : y;
    }

    public abstract List<Pair<HexPattern, HexCoord>> getPatterns(UnaryOperator<IVariable> lookup);

    public abstract boolean showStrokeOrder();

    @Override
    public void render(PoseStack poseStack, IComponentRenderContext ctx, float partialTicks, int mouseX, int mouseY) {
        PatternDrawingUtil.drawPattern(poseStack, this.x, this.y, this.patterns, this.pathfinderDots,
            this.showStrokeOrder(), ctx.getTicksInBook(),
            0xff_d2c8c8, 0xc8_aba2a2, 0xc8_322b33, 0x80_d1cccc);
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        var data = PatternDrawingUtil.loadPatterns(this.getPatterns(lookup));
        this.hexSize = data.hexSize();
        this.patterns = data.patterns();
        this.pathfinderDots = data.pathfinderDots();
    }

    protected static class RawPattern {
        protected String startdir;
        protected String signature;
        protected int q, r;
    }
}
