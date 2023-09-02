package at.petrak.hexcasting.client;

import at.petrak.hexcasting.client.gui.PatternTooltipGreeble;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;

// https://github.com/VazkiiMods/Quark/blob/ace90bfcc26db4c50a179f026134e2577987c2b1/src/main/java/vazkii/quark/content/client/module/ImprovedTooltipsModule.java
public class HexTooltips {
    public static void init() {
        IClientXplatAbstractions.INSTANCE.registerIdentityTooltipMapping(PatternTooltipGreeble.class);
    }
}
