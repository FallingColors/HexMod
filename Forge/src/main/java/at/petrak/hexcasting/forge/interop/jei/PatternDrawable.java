package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.client.render.PatternColors;
import at.petrak.hexcasting.client.render.PatternRenderer;
import at.petrak.hexcasting.client.render.PatternSettings;
import at.petrak.hexcasting.client.render.PatternSettings.PositionSettings;
import at.petrak.hexcasting.client.render.PatternSettings.StrokeSettings;
import at.petrak.hexcasting.client.render.PatternSettings.ZappySettings;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class PatternDrawable implements IDrawable {
    private final int width;
    private final int height;

    private boolean strokeOrder;
    private final HexPattern pat;

    private PatternSettings patSets;

    public PatternDrawable(ResourceLocation pattern, int w, int h) {
        var regi = IXplatAbstractions.INSTANCE.getActionRegistry();
        var entry = regi.get(pattern);
        this.strokeOrder = !HexUtils.isOfTag(regi, pattern, HexTags.Actions.PER_WORLD_PATTERN);
        this.pat = entry.prototype();
        this.width = w;
        this.height = h;
        this.patSets = new PatternSettings("pattern_drawable_" + w + "_" + h,
                new PositionSettings(width, height, 0, 0,
                        PatternSettings.AxisAlignment.CENTER_FIT, PatternSettings.AxisAlignment.CENTER_FIT, Math.max(width, height), 0, 0),
                StrokeSettings.fromStroke(0.075 * Math.min(width, height)),
                ZappySettings.READABLE);
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
        if(order != strokeOrder){
            patSets = new PatternSettings("pattern_drawable_" + width + "_" + height + (order ? "" : "nostroke"),
                    patSets.posSets,
                    patSets.strokeSets,
                    order ? ZappySettings.READABLE : ZappySettings.STATIC
                    );
        }
        strokeOrder = order;
        return this;
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y) {
        var ps = graphics.pose();
        ps.pushPose();
        ps.translate(x, y + 1, 0);
        PatternRenderer.renderPattern(pat, graphics.pose(), patSets,
                new PatternColors(0xc8_0c0a0c, 0xff_333030).withDotColors(0x80_666363, 0),
                0, 10
        );
        ps.popPose();
    }
}
