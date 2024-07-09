package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.client.render.HexPatternPoints;
import at.petrak.hexcasting.client.render.PatternColors;
import at.petrak.hexcasting.client.render.PatternRenderSettings;
import at.petrak.hexcasting.client.render.PatternRenderer;
import com.samsthenerd.inline.api.client.InlineRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class InlinePatternRenderer implements InlineRenderer<InlinePatternData> {

    public static final InlinePatternRenderer INSTANCE = new InlinePatternRenderer();

    public ResourceLocation getId(){
        return InlinePatternData.rendererId;
    }

    public static final PatternRenderSettings INLINE_RENDER_SETTINGS = new PatternRenderSettings()
            .withSizings(PatternRenderSettings.FitAxis.VERT, 8.0, 9.0, 1.0, 0.5, 4.0, null, null,
                    (scale) -> 1f, null)
            .withZappySettings(null, 0f, 0f, 0f, 0f, null)
            .named("inline");

    public static final int INLINE_TEXTURE_RES = 8; // 64px is probably fine for such small images ?

    public static final PatternColors FLAT_WHITE_PATTERN_COLOR = new PatternColors(0xFF_000000);

    public int render(InlinePatternData data, GuiGraphics drawContext, int index, Style style, int codepoint, TextRenderingContext trContext){
        drawContext.pose().pushPose();
        drawContext.pose().translate(0f, -0.5f, 0f);
        int trColor = FastColor.ARGB32.color((int)(255*trContext.alpha), (int)(255*trContext.red),
                (int)(255*trContext.green), (int)(255*trContext.blue));
        int color = style.getColor() == null ? trColor : style.getColor().getValue();
        PatternRenderer.renderPattern(data.pattern, drawContext.pose(), null, INLINE_RENDER_SETTINGS,
                new PatternColors(color), 0, trContext.light, null, INLINE_TEXTURE_RES);

        drawContext.pose().popPose();
        return charWidth(data, style, codepoint);
    }

    public int charWidth(InlinePatternData data, Style style, int codepoint){

        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(data.pattern, INLINE_RENDER_SETTINGS, 0);

        double baseScale = 4.0 / 1.5;
        double baseHeight = staticPoints.rangeY * baseScale;

        return (int)Math.round(0.2 + Math.min(baseHeight, 8.0) * staticPoints.rangeX / staticPoints.rangeY) + 2; // (+2 for padding)
    }
}
