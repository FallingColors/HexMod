package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.common.lib.HexItems;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.extrahooks.ItemOverlayRenderer;

public class InlineHexClient {

    public static void init(){
        InlineClientAPI.INSTANCE.addMatcher(HexPatternMatcher.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlinePatternRenderer.INSTANCE);

        ItemOverlayRenderer.addRenderer(HexItems.SCROLL_LARGE.get(), HexPatternOverlayRenderer.SCROLL_RENDERER);
        ItemOverlayRenderer.addRenderer(HexItems.SCROLL_MEDIUM.get(), HexPatternOverlayRenderer.SCROLL_RENDERER);
        ItemOverlayRenderer.addRenderer(HexItems.SCROLL_SMOL.get(), HexPatternOverlayRenderer.SCROLL_RENDERER);
        ItemOverlayRenderer.addRenderer(HexItems.SLATE.get(), HexPatternOverlayRenderer.SLATE_RENDERER);
    }
}
