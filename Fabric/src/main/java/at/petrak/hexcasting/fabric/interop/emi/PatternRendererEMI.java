package at.petrak.hexcasting.fabric.interop.emi;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.interop.utils.PatternDrawingUtil;
import at.petrak.hexcasting.interop.utils.PatternEntry;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class PatternRendererEMI implements EmiRenderable {

    private final int width;
    private final int height;

    private int xOffset = 0;
    private int yOffset = 0;

    private boolean strokeOrder;

    private final List<PatternEntry> patterns;
    private final List<Vec2> pathfinderDots;

    public PatternRendererEMI(ResourceLocation pattern, int w, int h) {
        var regi = IXplatAbstractions.INSTANCE.getActionRegistry();
        var entry = regi.get(pattern);
        this.strokeOrder = HexUtils.isOfTag(regi, pattern, HexTags.Actions.PER_WORLD_PATTERN);
        var data = PatternDrawingUtil.loadPatterns(List.of(new Pair<>(entry.prototype(), HexCoord.getOrigin())), 0f,
            1f);
        this.patterns = data.patterns();
        this.pathfinderDots = data.pathfinderDots();
        this.width = w;
        this.height = h;
    }

    public PatternRendererEMI shift(int x, int y) {
        xOffset += x;
        yOffset += y;
        return this;
    }

    public PatternRendererEMI strokeOrder(boolean order) {
        strokeOrder = order;
        return this;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta) {
        var ps = graphics.pose();
        ps.pushPose();
        ps.translate(xOffset + x - 0.5f + width / 2f, yOffset + y + 1 + height / 2f, 0);
        ps.scale(width / 64f, height / 64f, 1f);
        PatternDrawingUtil.drawPattern(graphics, 0, 0, this.patterns, this.pathfinderDots, this.strokeOrder,
            0xff_333030, 0xff_191818, 0xc8_0c0a0c, 0x80_666363);
        ps.popPose();
    }
}
