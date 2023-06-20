package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.common.items.ItemJewelerHammer;
import at.petrak.hexcasting.common.items.ItemLens;
import at.petrak.hexcasting.common.items.ItemLoreFragment;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.pigment.ItemAmethystAndCopperPigment;
import at.petrak.hexcasting.common.items.pigment.ItemDyePigment;
import at.petrak.hexcasting.common.items.pigment.ItemPridePigment;
import at.petrak.hexcasting.common.items.pigment.ItemUUIDPigment;
import at.petrak.hexcasting.common.items.magic.*;
import at.petrak.hexcasting.common.items.storage.*;
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

    public static final Item QUENCHED_SHARD = make("quenched_allay_shard", new Item(props()));

    public static final ItemStaff STAFF_OAK = make("staff/oak", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_SPRUCE = make("staff/spruce", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_BIRCH = make("staff/birch", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_JUNGLE = make("staff/jungle", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_ACACIA = make("staff/acacia", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_DARK_OAK = make("staff/dark_oak", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_CRIMSON = make("staff/crimson", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_WARPED = make("staff/warped", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_MANGROVE = make("staff/mangrove", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_EDIFIED = make("staff/edified", new ItemStaff(unstackable()));
    public static final ItemStaff STAFF_QUENCHED = make("staff/quenched", new ItemStaff(unstackable()));
    // mindsplice staffaratus
    public static final ItemStaff STAFF_MINDSPLICE = make("staff/mindsplice", new ItemStaff(unstackable()));

    public static final ItemLens SCRYING_LENS = make("lens", new ItemLens(
        IXplatAbstractions.INSTANCE.addEquipSlotFabric(EquipmentSlot.HEAD)
            .stacksTo(1)));

    public static final ItemAbacus ABACUS = make("abacus", new ItemAbacus(unstackable()));
    public static final ItemThoughtKnot THOUGHT_KNOT = make("thought_knot", new ItemThoughtKnot(unstackable()));
    public static final ItemFocus FOCUS = make("focus", new ItemFocus(unstackable()));
    public static final ItemSpellbook SPELLBOOK = make("spellbook", new ItemSpellbook(unstackable()));

    public static final ItemCypher CYPHER = make("cypher", new ItemCypher(unstackable()));
    public static final ItemTrinket TRINKET = make("trinket", new ItemTrinket(unstackable().rarity(Rarity.UNCOMMON)));
    public static final ItemArtifact ARTIFACT = make("artifact", new ItemArtifact(unstackable().rarity(Rarity.RARE)));

    public static final ItemJewelerHammer JEWELER_HAMMER = make("jeweler_hammer",
        new ItemJewelerHammer(Tiers.IRON, 0, -2.8F, props().stacksTo(1).defaultDurability(Tiers.DIAMOND.getUses())));

    public static final ItemScroll SCROLL_SMOL = make("scroll_small", new ItemScroll(props(), 1));
    public static final ItemScroll SCROLL_MEDIUM = make("scroll_medium", new ItemScroll(props(), 2));
    public static final ItemScroll SCROLL_LARGE = make("scroll", new ItemScroll(props(), 3));

    public static final ItemSlate SLATE = make("slate", new ItemSlate(HexBlocks.SLATE, props()));

    public static final ItemMediaBattery BATTERY = make("battery",
        new ItemMediaBattery(unstackable()));

    public static final EnumMap<DyeColor, ItemDyePigment> DYE_PIGMENTS = Util.make(() -> {
        var out = new EnumMap<DyeColor, ItemDyePigment>(DyeColor.class);
        for (var dye : DyeColor.values()) {
            out.put(dye, make("dye_colorizer_" + dye.getName(), new ItemDyePigment(dye, unstackable())));
        }
        return out;
    });
    public static final EnumMap<ItemPridePigment.Type, ItemPridePigment> PRIDE_PIGMENTS = Util.make(() -> {
        var out = new EnumMap<ItemPridePigment.Type, ItemPridePigment>(ItemPridePigment.Type.class);
        for (var politicsInMyVidya : ItemPridePigment.Type.values()) {
            out.put(politicsInMyVidya, make("pride_colorizer_" + politicsInMyVidya.getName(),
                new ItemPridePigment(politicsInMyVidya, unstackable())));
        }
        return out;
    });

    public static final Item UUID_PIGMENT = make("uuid_colorizer", new ItemUUIDPigment(unstackable()));
    public static final Item DEFAULT_PIGMENT = make("default_colorizer",
        new ItemAmethystAndCopperPigment(unstackable()));

    // BUFF SANDVICH
    public static final Item SUBMARINE_SANDWICH = make("sub_sandwich",
        new Item(props().food(new FoodProperties.Builder().nutrition(14).saturationMod(1.2f).build())));

    public static final ItemLoreFragment LORE_FRAGMENT = make("lore_fragment",
        new ItemLoreFragment(unstackable()
            .rarity(Rarity.RARE)));

    public static final ItemCreativeUnlocker CREATIVE_UNLOCKER = make("creative_unlocker",
        new ItemCreativeUnlocker(unstackable()
            .rarity(Rarity.EPIC)
            .food(new FoodProperties.Builder().nutrition(20).saturationMod(1f).alwaysEat().build())));

    //

    public static Item.Properties props() {
        return new Item.Properties();
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
