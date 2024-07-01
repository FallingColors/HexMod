package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.common.lib.HexItems;
import com.samsthenerd.inline.api.client.InlineRenderer;
import com.samsthenerd.inline.api.client.renderers.InlineItemRenderer;
import com.samsthenerd.inline.api.data.ItemInlineData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class InlinePatternRenderer implements InlineRenderer<InlinePatternData> {

    public static final InlinePatternRenderer INSTANCE = new InlinePatternRenderer();

    public ResourceLocation getId(){
        return InlinePatternData.rendererId;
    }

    public int render(InlinePatternData data, GuiGraphics drawContext, int index, Style style, int codepoint, TextRenderingContext trContext){
        ItemStack scrollStack = new ItemStack(HexItems.SCROLL_MEDIUM);
        HexItems.SCROLL_MEDIUM.writeDatum(scrollStack, new PatternIota(data.pattern));
        // placeholder to test that matcher works
        return InlineItemRenderer.INSTANCE.render(new ItemInlineData(scrollStack), drawContext, index, style, codepoint, trContext);
    }

    public int charWidth(InlinePatternData data, Style style, int codepoint){
        ItemStack scrollStack = new ItemStack(HexItems.SCROLL_MEDIUM);
        HexItems.SCROLL_MEDIUM.writeDatum(scrollStack, new PatternIota(data.pattern));
        // placeholder to test that matcher works
        return InlineItemRenderer.INSTANCE.charWidth(new ItemInlineData(scrollStack), style, codepoint);
    }
}
