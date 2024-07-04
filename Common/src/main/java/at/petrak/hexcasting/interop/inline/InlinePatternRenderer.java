package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.client.render.HexPatternPoints;
import at.petrak.hexcasting.client.render.PatternColors;
import at.petrak.hexcasting.client.render.PatternRenderSettings;
import at.petrak.hexcasting.client.render.PatternRenderer;
import com.samsthenerd.inline.api.client.InlineRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class InlinePatternRenderer implements InlineRenderer<InlinePatternData> {

    public static final InlinePatternRenderer INSTANCE = new InlinePatternRenderer();

    public ResourceLocation getId(){
        return InlinePatternData.rendererId;
    }

    public static final PatternRenderSettings INLINE_RENDER_SETTINGS = new PatternRenderSettings()
            .withSizings(PatternRenderSettings.FitAxis.VERT, 8.0, 8.0, 1.0, 0.0, 4.0, null, null,
                    (scale) -> 1f, null)
            .named("inline");

    public static final PatternColors FLAT_WHITE_PATTERN_COLOR = new PatternColors(0xFF_FFFFFF);

    public int render(InlinePatternData data, GuiGraphics drawContext, int index, Style style, int codepoint, TextRenderingContext trContext){
        PatternRenderer.renderPattern(data.pattern, drawContext.pose(), INLINE_RENDER_SETTINGS, FLAT_WHITE_PATTERN_COLOR, 0);

        return charWidth(data, style, codepoint);
    }

    public int charWidth(InlinePatternData data, Style style, int codepoint){

        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(data.pattern, INLINE_RENDER_SETTINGS, 0);

        double baseScale = 4.0 / 1.5;
        double baseHeight = staticPoints.rangeY * baseScale;

        return (int)Math.round(0.2 + Math.min(baseHeight, 8.0) * staticPoints.rangeX / staticPoints.rangeY) + 2; // (+2 for padding)
    }
}
