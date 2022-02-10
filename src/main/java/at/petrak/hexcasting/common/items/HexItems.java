package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.items.colorizer.ItemDyeColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.items.magic.ItemArtifact;
import at.petrak.hexcasting.common.items.magic.ItemCypher;
import at.petrak.hexcasting.common.items.magic.ItemTrinket;
import at.petrak.hexcasting.common.lib.HexItemNames;
import net.minecraft.core.NonNullList;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HexMod.MOD_ID);
    public static final CreativeModeTab TAB = new CreativeModeTab(HexMod.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(SPELLBOOK::get);
        }

        @Override
        public void fillItemList(NonNullList<ItemStack> items) {
            super.fillItemList(items);
        }
    };

    public static final RegistryObject<ItemWand> WAND = ITEMS.register(HexItemNames.WAND,
        () -> new ItemWand(unstackable()));

    public static final RegistryObject<Item> AMETHYST_DUST = ITEMS.register(HexItemNames.AMETHYST_DUST,
        () -> new Item(props()));
    public static final RegistryObject<Item> CHARGED_AMETHYST = ITEMS.register(HexItemNames.CHARGED_AMETHYST,
        () -> new Item(props()));

    public static final RegistryObject<Item> SCRYING_LENS = ITEMS.register(HexItemNames.LENS,
        () -> new Item(unstackable()));
    public static final RegistryObject<Item> SCROLL = ITEMS.register(HexItemNames.SCROLL,
        () -> new ItemScroll(unstackable()));

    public static final RegistryObject<ItemFocus> FOCUS = ITEMS.register(HexItemNames.FOCUS,
        () -> new ItemFocus(props()));
    public static final RegistryObject<ItemSpellbook> SPELLBOOK = ITEMS.register(HexItemNames.SPELLBOOK,
        () -> new ItemSpellbook(unstackable()));

    public static final RegistryObject<ItemCypher> CYPHER = ITEMS.register(HexItemNames.CYPHER,
        () -> new ItemCypher(unstackable()));
    public static final RegistryObject<ItemTrinket> TRINKET = ITEMS.register(HexItemNames.TRINKET,
        () -> new ItemTrinket(unstackable()));
    public static final RegistryObject<ItemArtifact> ARTIFACT = ITEMS.register(HexItemNames.ARTIFACT,
        () -> new ItemArtifact(unstackable()));

    public static final RegistryObject<ItemDyeColorizer>[] DYE_COLORIZERS = new RegistryObject[16];
    public static final RegistryObject<ItemPrideColorizer>[] PRIDE_COLORIZERS = new RegistryObject[14];

    static {
        for (int i = 0; i < DYE_COLORIZERS.length; i++) {
            var dye = DyeColor.values()[i];
            final var finalI = i;
            DYE_COLORIZERS[i] = ITEMS.register(HexItemNames.DYE_COLORIZER_STUB + dye.getName(),
                () -> new ItemDyeColorizer(finalI, unstackable()));
        }
        for (int i = 0; i < PRIDE_COLORIZERS.length; i++) {
            final var finalI = i;
            PRIDE_COLORIZERS[i] = ITEMS.register(HexItemNames.PRIDE_COLORIZER_STUB + i,
                () -> new ItemPrideColorizer(finalI, unstackable()));
        }
    }

    public static final RegistryObject<Item> UUID_COLORIZER = ITEMS.register(HexItemNames.UUID_COLORIZER,
        () -> new Item(unstackable()));

    // BUFF SANDVICH
    public static final RegistryObject<Item> SUBMARINE_SANDWICH = ITEMS.register(HexItemNames.SUBMARINE_SANDWICH,
        () -> new Item(props().food(new FoodProperties.Builder().nutrition(14).saturationMod(1.2f).build())));

    public static Item.Properties props() {
        return new Item.Properties().tab(TAB);
    }

    public static Item.Properties unstackable() {
        return props().stacksTo(1);
    }
}
