package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.items.*;
import at.petrak.hexcasting.common.items.colorizer.ItemDyeColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemPrideColorizer;
import at.petrak.hexcasting.common.items.colorizer.ItemUUIDColorizer;
import at.petrak.hexcasting.common.items.magic.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/2c4f7fdf9ebf0c0afa1406dfe1322841133d75fa/Common/src/main/java/vazkii/botania/common/item/ModItems.java
public class HexItems {
    public static void registerItems(BiConsumer<Item, ResourceLocation> r) {
        for (var e : ITEMS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, Item> ITEMS = new LinkedHashMap<>(); // preserve insertion order

    public static final Item AMETHYST_DUST = make("amethyst_dust", new Item(props()));
    public static final Item CHARGED_AMETHYST = make("charged_amethyst", new Item(props()));

    public static final ItemWand WAND_OAK = make("wand_oak", new ItemWand(unstackable()));
    public static final ItemWand WAND_SPRUCE = make("wand_spruce", new ItemWand(unstackable()));
    public static final ItemWand WAND_BIRCH = make("wand_birch", new ItemWand(unstackable()));
    public static final ItemWand WAND_JUNGLE = make("wand_jungle", new ItemWand(unstackable()));
    public static final ItemWand WAND_ACACIA = make("wand_acacia", new ItemWand(unstackable()));
    public static final ItemWand WAND_DARK_OAK = make("wand_dark_oak", new ItemWand(unstackable()));
    public static final ItemWand WAND_CRIMSON = make("wand_crimson", new ItemWand(unstackable()));
    public static final ItemWand WAND_WARPED = make("wand_warped", new ItemWand(unstackable()));
    public static final ItemWand WAND_AKASHIC = make("wand_akashic", new ItemWand(unstackable()));

    public static final ItemLens SCRYING_LENS = make("lens", new ItemLens(
        IXplatAbstractions.INSTANCE.addEquipSlotFabric(EquipmentSlot.HEAD)
            .stacksTo(1)
            .tab(IXplatAbstractions.INSTANCE.getTab())));

    public static final ItemAbacus ABACUS = make("abacus", new ItemAbacus(unstackable()));
    public static final ItemFocus FOCUS = make("focus", new ItemFocus(unstackable()));
    public static final ItemSpellbook SPELLBOOK = make("spellbook", new ItemSpellbook(unstackable()));

    public static final ItemCypher CYPHER = make("cypher", new ItemCypher(unstackable()));
    public static final ItemTrinket TRINKET = make("trinket", new ItemTrinket(unstackable()));
    public static final ItemArtifact ARTIFACT = make("artifact", new ItemArtifact(unstackable()));

    public static final ItemJewelerHammer JEWELER_HAMMER = make("jeweler_hammer",
        new ItemJewelerHammer(Tiers.IRON, 0, -2.8F, props().stacksTo(1).defaultDurability(Tiers.DIAMOND.getUses())));

    public static final ItemScroll SCROLL_SMOL = make("scroll_small", new ItemScroll(props(), 1));
    public static final ItemScroll SCROLL_MEDIUM = make("scroll_medium", new ItemScroll(props(), 2));
    public static final ItemScroll SCROLL_LARGE = make("scroll", new ItemScroll(props(), 3));

    public static final ItemSlate SLATE = make("slate", new ItemSlate(HexBlocks.SLATE, props()));

    public static final ItemManaBattery BATTERY = make("battery",
        new ItemManaBattery(unstackable()));

    public static final EnumMap<DyeColor, ItemDyeColorizer> DYE_COLORIZERS = Util.make(() -> {
        var out = new EnumMap<DyeColor, ItemDyeColorizer>(DyeColor.class);
        for (var dye : DyeColor.values()) {
            out.put(dye, make("dye_colorizer_" + dye.getName(), new ItemDyeColorizer(dye, unstackable())));
        }
        return out;
    });
    public static final EnumMap<ItemPrideColorizer.Type, ItemPrideColorizer> PRIDE_COLORIZERS = Util.make(() -> {
        var out = new EnumMap<ItemPrideColorizer.Type, ItemPrideColorizer>(ItemPrideColorizer.Type.class);
        for (var politicsInMyVidya : ItemPrideColorizer.Type.values()) {
            out.put(politicsInMyVidya, make("pride_colorizer_" + politicsInMyVidya.getName(),
                new ItemPrideColorizer(politicsInMyVidya, unstackable())));
        }
        return out;
    });

    public static final Item UUID_COLORIZER = make("uuid_colorizer", new ItemUUIDColorizer(unstackable()));

    // BUFF SANDVICH
    public static final Item SUBMARINE_SANDWICH = make("sub_sandwich",
        new Item(props().food(new FoodProperties.Builder().nutrition(14).saturationMod(1.2f).build())));

    public static final ItemCreativeUnlocker CREATIVE_UNLOCKER = make("creative_unlocker",
        new ItemCreativeUnlocker(unstackable()
            .rarity(Rarity.EPIC)
            .food(new FoodProperties.Builder().nutrition(20).saturationMod(1f).alwaysEat().build())));

    //

    public static Item.Properties props() {
        return new Item.Properties().tab(IXplatAbstractions.INSTANCE.getTab());
    }

    public static Item.Properties unstackable() {
        return props().stacksTo(1);
    }

    private static <T extends Item> T make(ResourceLocation id, T item) {
        var old = ITEMS.put(id, item);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return item;
    }

    private static <T extends Item> T make(String id, T item) {
        return make(modLoc(id), item);
    }

    public static ItemStack tabIcon() {
        return new ItemStack(SPELLBOOK);
    }
}
