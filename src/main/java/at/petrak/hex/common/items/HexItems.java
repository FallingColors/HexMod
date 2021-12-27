package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.lib.LibItemNames;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HexMod.MOD_ID);

    public static final RegistryObject<Item> WAND = ITEMS.register(LibItemNames.WAND,
            () -> new ItemWand(unstackable()));
    public static final RegistryObject<Item> FOCUS = ITEMS.register(LibItemNames.FOCUS,
            () -> new ItemFocus(props()));

    public static Item.Properties props() {
        return new Item.Properties();
    }

    public static Item.Properties unstackable() {
        return props().stacksTo(1);
    }
}
