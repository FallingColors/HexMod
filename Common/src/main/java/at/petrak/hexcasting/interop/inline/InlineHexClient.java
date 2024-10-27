package at.petrak.hexcasting.interop.inline;

import com.samsthenerd.inline.api.client.InlineClientAPI;

public class InlineHexClient {

    public static void init(){
        InlineClientAPI.INSTANCE.addMatcher(HexPatternMatcher.INSTANCE);
        InlineClientAPI.INSTANCE.addRenderer(InlinePatternRenderer.INSTANCE);
    }
}
