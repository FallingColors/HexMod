package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.interop.utils.PatternDrawingUtil;
import at.petrak.hexcasting.interop.utils.PatternEntry;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class PatternDrawable implements IDrawable {
    private final int width;
    private final int height;

    private boolean strokeOrder;

    private final List<PatternEntry> patterns;
    private final List<Vec2> pathfinderDots;

    public PatternDrawable(ResourceLocation pattern, int w, int h) {
        var regi = IXplatAbstractions.INSTANCE.getActionRegistry();
        var entry = regi.get(pattern);
        this.strokeOrder = !HexUtils.isOfTag(regi, pattern, HexTags.Actions.PER_WORLD_PATTERN);
        var data = PatternDrawingUtil.loadPatterns(
            List.of(new Pair<>(entry.prototype(), HexCoord.getOrigin())),
            0f,
            1f);
        this.patterns = data.patterns();
        this.pathfinderDots = data.pathfinderDots();
        this.width = w;
        this.height = h;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public PatternDrawable strokeOrder(boolean order) {
        strokeOrder = order;
        return this;
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        var ps = guiGraphics.pose();
        ps.pushPose();
        ps.translate(xOffset - 0.5f + width / 2f, yOffset + height / 2f, 0);
        ps.scale(width / 64f, height / 64f, 1f);
        PatternDrawingUtil.drawPattern(guiGraphics, 0, 0, this.patterns, this.pathfinderDots, this.strokeOrder,
                0xff_333030, 0xff_191818, 0xc8_0c0a0c, 0x80_666363);
        ps.popPose();
    }
}
