package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.items.colorizer.ItemDyeColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.items.magic.ItemArtifact;
import at.petrak.hexcasting.common.items.magic.ItemCypher;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.items.magic.ItemTrinket;
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

            var manamounts = new int[]{
                100_000,
                1_000_000,
                10_000_000,
                100_000_000,
                1_000_000_000,
            };
            for (int manamount : manamounts) {
                var stack = new ItemStack(BATTERY.get(), 1);
                var tag = stack.getOrCreateTag();
                tag.putInt(ItemManaBattery.TAG_MANA, manamount);
                tag.putInt(ItemManaBattery.TAG_MAX_MANA, manamount);
                items.add(stack);
            }
        }
    };

    public static final RegistryObject<ItemWand> WAND = ITEMS.register("wand",
        () -> new ItemWand(unstackable()));

    public static final RegistryObject<Item> AMETHYST_DUST = ITEMS.register("amethyst_dust",
        () -> new Item(props()));
    public static final RegistryObject<Item> CHARGED_AMETHYST = ITEMS.register("charged_amethyst",
        () -> new Item(props()));

    public static final RegistryObject<Item> SCRYING_LENS = ITEMS.register("lens",
        () -> new Item(unstackable()));
    public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll",
        () -> new ItemScroll(props()));

    public static final RegistryObject<ItemFocus> FOCUS = ITEMS.register("focus",
        () -> new ItemFocus(unstackable()));
    public static final RegistryObject<ItemAbacus> ABACUS = ITEMS.register("abacus",
        () -> new ItemAbacus(unstackable()));
    public static final RegistryObject<ItemSpellbook> SPELLBOOK = ITEMS.register("spellbook",
        () -> new ItemSpellbook(unstackable()));

    public static final RegistryObject<ItemCypher> CYPHER = ITEMS.register("cypher",
        () -> new ItemCypher(unstackable()));
    public static final RegistryObject<ItemTrinket> TRINKET = ITEMS.register("trinket",
        () -> new ItemTrinket(unstackable()));
    public static final RegistryObject<ItemArtifact> ARTIFACT = ITEMS.register("artifact",
        () -> new ItemArtifact(unstackable()));

    public static final RegistryObject<ItemManaBattery> BATTERY = ITEMS.register("battery",
        () -> new ItemManaBattery(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<ItemDyeColorizer>[] DYE_COLORIZERS = new RegistryObject[16];
    public static final RegistryObject<ItemPrideColorizer>[] PRIDE_COLORIZERS = new RegistryObject[14];

    static {
        for (int i = 0; i < DYE_COLORIZERS.length; i++) {
            var dye = DyeColor.values()[i];
            final var finalI = i;
            DYE_COLORIZERS[i] = ITEMS.register("dye_colorizer_" + dye.getName(),
                () -> new ItemDyeColorizer(finalI, unstackable()));
        }
        for (int i = 0; i < PRIDE_COLORIZERS.length; i++) {
            final var finalI = i;
            PRIDE_COLORIZERS[i] = ITEMS.register("pride_colorizer_" + i,
                () -> new ItemPrideColorizer(finalI, unstackable()));
        }
    }

    public static final RegistryObject<Item> UUID_COLORIZER = ITEMS.register("uuid_colorizer",
        () -> new Item(unstackable()));

    // BUFF SANDVICH
    public static final RegistryObject<Item> SUBMARINE_SANDWICH = ITEMS.register("sub_sandwich",
        () -> new Item(props().food(new FoodProperties.Builder().nutrition(14).saturationMod(1.2f).build())));

    public static final RegistryObject<ItemSlate> SLATE = ITEMS.register("slate",
        () -> new ItemSlate(HexBlocks.SLATE.get(), props()));

    public static Item.Properties props() {
        return new Item.Properties().tab(TAB);
    }

    public static Item.Properties unstackable() {
        return props().stacksTo(1);
    }
}
