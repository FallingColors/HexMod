package at.petrak.hexcasting.common.lib;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import at.petrak.hexcasting.common.items.storage.ItemScroll;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.MOD_ID;
import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexCreativeTabs {
    public static void registerCreativeTabs(BiConsumer<CreativeModeTab, ResourceLocation> r) {
        for (var e : TABS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, CreativeModeTab> TABS = new LinkedHashMap<>();

    public static final CreativeModeTab HEX = register("hexcasting", CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(HexItems.SPELLBOOK)));

    public static final ResourceKey<CreativeModeTab> HEX_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), modLoc("hexcasting"));

    public static final CreativeModeTab SCROLLS = register("scrolls", CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(HexItems.SCROLL_LARGE)));

    public static final ResourceKey<CreativeModeTab> SCROLLS_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), modLoc("scrolls"));

    private static CreativeModeTab register(String name, CreativeModeTab.Builder tabBuilder) {
        var tab = tabBuilder.title(Component.translatable("itemGroup.hexcasting." + name)).build();
        var old = TABS.put(modLoc(name), tab);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return tab;
    }
}
