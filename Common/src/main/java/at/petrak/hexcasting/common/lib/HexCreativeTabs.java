package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexCreativeTabs {
    private static final IXplatRegister<CreativeModeTab> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(Registries.CREATIVE_MODE_TAB);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Holder<CreativeModeTab> HEX = REGISTER.registerHolder("hexcasting", () ->
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(HexItems.SPELLBOOK.get()))
                    .title(Component.translatable("itemGroup.hexcasting.hexcasting"))
                    .build());

    public static final ResourceKey<CreativeModeTab> HEX_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), modLoc("hexcasting"));

    public static final Holder<CreativeModeTab> SCROLLS = REGISTER.registerHolder("scrolls", () ->
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(HexItems.SCROLL_LARGE.get()))
                    .title(Component.translatable("itemGroup.hexcasting.scrolls"))
                    .build());

    public static final ResourceKey<CreativeModeTab> SCROLLS_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), modLoc("scrolls"));
}
