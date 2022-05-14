package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.blocks.HexBlocks;
import at.petrak.hexcasting.common.items.colorizer.ItemDyeColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemUUIDColorizer;
import at.petrak.hexcasting.common.items.magic.ItemArtifact;
import at.petrak.hexcasting.common.items.magic.ItemCypher;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.items.magic.ItemTrinket;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

public class HexItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HexMod.MOD_ID);
    public static final CreativeModeTab TAB = new CreativeModeTab(HexMod.MOD_ID) {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(SPELLBOOK::get);
        }
    };

    public static final RegistryObject<ItemWand> WAND_OAK = ITEMS.register("wand_oak",
        () -> new ItemWand(unstackable()));
    public static final RegistryObject<ItemWand> WAND_SPRUCE = ITEMS.register("wand_spruce",
        () -> new ItemWand(unstackable()));
    public static final RegistryObject<ItemWand> WAND_BIRCH = ITEMS.register("wand_birch",
        () -> new ItemWand(unstackable()));
    public static final RegistryObject<ItemWand> WAND_JUNGLE = ITEMS.register("wand_jungle",
        () -> new ItemWand(unstackable()));
    public static final RegistryObject<ItemWand> WAND_ACACIA = ITEMS.register("wand_acacia",
        () -> new ItemWand(unstackable()));
    public static final RegistryObject<ItemWand> WAND_DARK_OAK = ITEMS.register("wand_dark_oak",
        () -> new ItemWand(unstackable()));
    public static final RegistryObject<ItemWand> WAND_CRIMSON = ITEMS.register("wand_crimson",
        () -> new ItemWand(unstackable()));
    public static final RegistryObject<ItemWand> WAND_WARPED = ITEMS.register("wand_warped",
        () -> new ItemWand(unstackable()));
    public static final RegistryObject<ItemWand> WAND_AKASHIC = ITEMS.register("wand_akashic",
        () -> new ItemWand(unstackable()));

    public static final RegistryObject<Item> AMETHYST_DUST = ITEMS.register("amethyst_dust",
        () -> new Item(props()));
    public static final RegistryObject<Item> CHARGED_AMETHYST = ITEMS.register("charged_amethyst",
        () -> new Item(props()));

    public static final RegistryObject<Item> SCRYING_LENS = ITEMS.register("lens",
        () -> new ItemLens(unstackable()));
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
        () -> new ItemManaBattery(props().stacksTo(1)));

    public static final RegistryObject<PickaxeItem> JEWELER_HAMMER = ITEMS.register("jeweler_hammer",
        () -> new PickaxeItem(Tiers.IRON, 0, -2.8F, props().stacksTo(1)));

    public static final EnumMap<DyeColor, RegistryObject<ItemDyeColorizer>> DYE_COLORIZERS = new EnumMap<>(
        DyeColor.class);
    public static final RegistryObject<ItemPrideColorizer>[] PRIDE_COLORIZERS = new RegistryObject[14];

    static {
        for (var dye : DyeColor.values()) {
            DYE_COLORIZERS.put(dye, ITEMS.register("dye_colorizer_" + dye.getName(),
                () -> new ItemDyeColorizer(dye, unstackable())));
        }
        for (int i = 0; i < PRIDE_COLORIZERS.length; i++) {
            final var finalI = i;
            PRIDE_COLORIZERS[i] = ITEMS.register("pride_colorizer_" + i,
                () -> new ItemPrideColorizer(finalI, unstackable()));
        }
    }

    public static final RegistryObject<Item> UUID_COLORIZER = ITEMS.register("uuid_colorizer",
        () -> new ItemUUIDColorizer(unstackable()));

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
