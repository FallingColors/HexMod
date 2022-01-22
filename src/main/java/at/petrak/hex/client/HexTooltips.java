package at.petrak.hex.client;

import at.petrak.hex.common.items.ItemScroll;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.client.MinecraftForgeClient;

import java.util.function.Function;

// https://github.com/VazkiiMods/Quark/blob/ace90bfcc26db4c50a179f026134e2577987c2b1/src/main/java/vazkii/quark/content/client/module/ImprovedTooltipsModule.java
public class HexTooltips {
    public static void init() {
        register(ItemScroll.TooltipGreeble.class);
    }

    private static <T extends ClientTooltipComponent & TooltipComponent> void register(Class<T> clazz) {
        MinecraftForgeClient.registerTooltipComponentFactory(clazz, Function.identity());
    }
}
