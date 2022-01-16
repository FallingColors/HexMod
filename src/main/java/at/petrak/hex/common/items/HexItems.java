package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.items.magic.ItemArtifact;
import at.petrak.hex.common.items.magic.ItemCypher;
import at.petrak.hex.common.items.magic.ItemTrinket;
import at.petrak.hex.common.lib.LibItemNames;
import net.minecraft.core.NonNullList;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HexMod.MOD_ID);
    public static final CreativeModeTab TAB = new CreativeModeTab("hex") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(SPELLBOOK::get);
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            super.fillItemList(items);
        }
    };

    public static final RegistryObject<Item> WAND = ITEMS.register(LibItemNames.WAND,
            () -> new ItemWand(unstackable()));
    public static final RegistryObject<Item> FOCUS = ITEMS.register(LibItemNames.FOCUS,
            () -> new ItemFocus(props()));
    public static final RegistryObject<Item> SPELLBOOK = ITEMS.register(LibItemNames.SPELLBOOK,
            () -> new ItemSpellbook(unstackable()));
    public static final RegistryObject<Item> CYPHER = ITEMS.register(LibItemNames.CYPHER,
            () -> new ItemCypher(unstackable()));
    public static final RegistryObject<Item> TRINKET = ITEMS.register(LibItemNames.TRINKET,
            () -> new ItemTrinket(unstackable()));
    public static final RegistryObject<Item> ARTIFACT = ITEMS.register(LibItemNames.ARTIFACT,
            () -> new ItemArtifact(unstackable()));
    public static final RegistryObject<Item> AMETHYST_DUST = ITEMS.register(LibItemNames.AMETHYST_DUST,
            () -> new Item(props()));
    public static final RegistryObject<Item> CHARGED_AMETHYST = ITEMS.register(LibItemNames.CHARGED_AMETHYST,
            () -> new Item(props()));
    // I am very funny
    public static final RegistryObject<Item> SUBMARINE_SANDWICH = ITEMS.register(LibItemNames.SUBMARINE_SANDWICH,
            () -> new Item(props().food(new FoodProperties.Builder().nutrition(4).build())));
    public static final RegistryObject<Item> SCRYING_LENS = ITEMS.register(LibItemNames.LENS,
            () -> new Item(unstackable()));

    public static Item.Properties props() {
        return new Item.Properties().tab(TAB);
    }

    public static Item.Properties unstackable() {
        return props().stacksTo(1);
    }
}
