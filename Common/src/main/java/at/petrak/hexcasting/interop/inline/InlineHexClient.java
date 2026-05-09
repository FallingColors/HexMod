package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.common.lib.HexItems;
import com.samsthenerd.inline.api.client.InlineClientAPI;
import com.samsthenerd.inline.api.client.extrahooks.ItemOverlayRenderer;

public class InlineHexClient {

    public static void init(){
        InlineClientAPI.INSTANCE.addMatcher(HexPatternMatcher.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlinePatternRenderer.INSTANCE);

        ItemOverlayRenderer.registerRenderer(HexItems.SCROLL_LARGE, HexPatternOverlayRenderer.SCROLL_RENDERER);
        ItemOverlayRenderer.registerRenderer(HexItems.SCROLL_MEDIUM, HexPatternOverlayRenderer.SCROLL_RENDERER);
        ItemOverlayRenderer.registerRenderer(HexItems.SCROLL_SMOL, HexPatternOverlayRenderer.SCROLL_RENDERER);
        ItemOverlayRenderer.registerRenderer(HexItems.SLATE, HexPatternOverlayRenderer.SLATE_RENDERER);
    }
}
