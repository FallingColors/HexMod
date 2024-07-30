package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.client.render.*;
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

    public static final PatternSettings INLINE_SETTINGS = new PatternSettings("inline",
            new PatternSettings.PositionSettings(1.0, 9.0, 0, 0.5,
                    PatternSettings.AxisAlignment.CENTER, PatternSettings.AxisAlignment.CENTER_FIT, 4.0, 0, 0),
            PatternSettings.StrokeSettings.fromStroke(1.0),
            new PatternSettings.ZappySettings(10, 0, 0, 0,
                    PatternSettings.ZappySettings.READABLE_OFFSET, 0.8f)
    ){
        @Override
        public double getOuterWidth(double scale){
            if(scale >= 1) return 1;
            if(scale >= 0.75) return 0.75;
            if(scale >= 0.5) return 0.5;
            return 0.25;
        }
    };

    public static final int INLINE_TEXTURE_RES = 16; // 128px so it looks good and pretty on up close signs and whatnot

    public int render(InlinePatternData data, GuiGraphics drawContext, int index, Style style, int codepoint, TextRenderingContext trContext){
        drawContext.pose().pushPose();
        drawContext.pose().translate(0f, -0.5f, 0f);
        int trColor = FastColor.ARGB32.color((int)(255*trContext.alpha), (int)(255*trContext.red),
                (int)(255*trContext.green), (int)(255*trContext.blue));
        int color = style.getColor() == null ? trColor : style.getColor().getValue();
        // some places (like tooltips) give an alpha of 0, but we don't want to kill the alpha value entirely.
        if(FastColor.ARGB32.alpha(color) == 0){
            color |= 0xFF_000000;
        }
        PatternRenderer.renderPattern(data.pattern, drawContext.pose(), new PatternRenderer.WorldlyBits(trContext.vertexConsumers, trContext.light, null),
                INLINE_SETTINGS, PatternColors.singleStroke(color), 0, INLINE_TEXTURE_RES);

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
