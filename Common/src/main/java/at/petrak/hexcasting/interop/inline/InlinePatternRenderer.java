package at.petrak.hexcasting.interop.inline;

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
            .withSizings(PatternRenderSettings.FitAxis.VERT, 8.0, 8.0, 0.0, 0.0, 4.0, null, null)
            .withColors(0xFF_FFFFFF, 0xFF_FFFFFF, null, null);

    public int render(InlinePatternData data, GuiGraphics drawContext, int index, Style style, int codepoint, TextRenderingContext trContext){
        PatternRenderer.renderPattern(data.pattern, drawContext.pose(), INLINE_RENDER_SETTINGS, 0);

        return (int)(8 * PatternRenderer.getPatternWHRatio(data.pattern, INLINE_RENDER_SETTINGS, 0));
    }

    public int charWidth(InlinePatternData data, Style style, int codepoint){
        return (int)(8 * PatternRenderer.getPatternWHRatio(data.pattern, INLINE_RENDER_SETTINGS, 0));
    }
}
