package at.petrak.hexcasting.common.lib;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import at.petrak.hexcasting.common.items.storage.ItemScroll;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexCreativeTabs {
    public static void registerCreativeTabs(BiConsumer<CreativeModeTab, ResourceLocation> r) {
        for (var e : TABS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, CreativeModeTab> TABS = new LinkedHashMap<>();

    public static final CreativeModeTab MAIN = register("hexcasting", CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
            .icon(() -> new ItemStack(HexItems.SPELLBOOK)));

    public static final CreativeModeTab SCROLLS = register("scrolls", CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
            .icon(() -> ItemScroll.withPerWorldPattern(new ItemStack(HexItems.SCROLL_LARGE),"")));

    private static CreativeModeTab register(String name, CreativeModeTab.Builder tabBuilder) {
        var tab = tabBuilder.title(Component.translatable("itemGroup.hexcasting." + name)).build();
        var old = TABS.put(modLoc(name), tab);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return tab;
    }
}
