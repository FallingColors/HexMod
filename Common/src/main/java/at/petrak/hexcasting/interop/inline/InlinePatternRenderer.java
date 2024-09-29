package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.client.render.*;
import com.samsthenerd.inline.api.client.GlowHandling;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.impl.InlineStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class InlinePatternRenderer implements InlineRenderer<InlinePatternData> {

    public static final InlinePatternRenderer INSTANCE = new InlinePatternRenderer();

    public ResourceLocation getId(){
        return InlinePatternData.rendererId;
    }

    public static final PatternSettings INLINE_SETTINGS = new PatternSettings("inline",
            new PatternSettings.PositionSettings(1.0, 9.0, 0, 0.5,
                    PatternSettings.AxisAlignment.CENTER, PatternSettings.AxisAlignment.CENTER_FIT, 4.0, 0, 0),
            PatternSettings.StrokeSettings.fromStroke(1.0),
            new PatternSettings.ZappySettings(10, 0, 0, 0,
                    PatternSettings.ZappySettings.READABLE_OFFSET, 0.7f)
    ){
        @Override
        public double getOuterWidth(double scale){
            if(scale >= 1) return 1;
            if(scale >= 0.75) return 0.75;
            if(scale >= 0.5) return 0.5;
            return 0.25;
        }
    };

    public static final PatternSettings INLINE_SETTINGS_GLOWY = new PatternSettings("inlineglowy",
            new PatternSettings.PositionSettings(1.0, 11.0, 0, 0.5,
                    PatternSettings.AxisAlignment.CENTER, PatternSettings.AxisAlignment.CENTER_FIT, 4.0, 0, 0),
            new PatternSettings.StrokeSettings(1, 3, 0.8 * 1 * 2.0 / 5.0, 0.4 * 1 * 2.0 / 5.0),
            INLINE_SETTINGS.zapSets
    ){
        @Override
        public double getInnerWidth(double scale){
            if(scale >= 1) return 1;
            if(scale >= 0.75) return 0.75;
            if(scale >= 0.5) return 0.5;
            return 0.25;
        }
    };

    @Override
    public GlowHandling getGlowPreference(InlinePatternData forData) {
        return new GlowHandling.None();
    }

    public static final int INLINE_TEXTURE_RES = 16; // 128px so it looks good and pretty on up close signs and whatnot

    public int render(InlinePatternData data, GuiGraphics drawContext, int index, Style style, int codepoint, TextRenderingContext trContext){
        if(trContext.isGlowy()) return charWidth(data, style, codepoint);
        int glowyParentColor = ((InlineStyle) style).getComponent(InlineStyle.GLOWY_PARENT_COMP);
        boolean isGlowy = glowyParentColor != -1;
        drawContext.pose().pushPose();
        drawContext.pose().translate(isGlowy ? -1f : 0, isGlowy ? -1.5f : -0.5f, 0f);
        int color = trContext.usableColor();
        PatternRenderer.renderPattern(data.pattern, drawContext.pose(), new PatternRenderer.WorldlyBits(drawContext.bufferSource(), trContext.light(), null),
                isGlowy ? INLINE_SETTINGS_GLOWY : INLINE_SETTINGS,
                isGlowy ? new PatternColors(color, 0xFF_000000 | glowyParentColor) : PatternColors.singleStroke(color),
                0, INLINE_TEXTURE_RES);

        drawContext.pose().popPose();
        return charWidth(data, style, codepoint);
    }

    public int charWidth(InlinePatternData data, Style style, int codepoint){

        HexPatternPoints staticPoints = HexPatternPoints.getStaticPoints(HexPatternLike.of(data.pattern), INLINE_SETTINGS, 0);

        double baseScale = 4.0 / 1.5;
        double baseHeight = staticPoints.rangeY * baseScale;

        return (int)Math.ceil(Math.min(baseHeight, 8.0) * staticPoints.rangeX / staticPoints.rangeY) + 1; // (+2 for padding)
    }
}
