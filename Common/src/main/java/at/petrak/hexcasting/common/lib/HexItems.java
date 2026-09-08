package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.items.ItemJewelerHammer;
import at.petrak.hexcasting.common.items.ItemLens;
import at.petrak.hexcasting.common.items.ItemLoreFragment;
import at.petrak.hexcasting.common.items.ItemStaff;
import at.petrak.hexcasting.common.items.armor.ItemRobes;
import at.petrak.hexcasting.common.items.magic.*;
import at.petrak.hexcasting.common.items.pigment.*;
import at.petrak.hexcasting.common.items.storage.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import com.google.common.base.Suppliers;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/2c4f7fdf9ebf0c0afa1406dfe1322841133d75fa/Common/src/main/java/vazkii/botania/common/item/ModItems.java
public class HexItems {
    private static final IXplatRegister<Item> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(Registries.ITEM);

    public static void register() {
        REGISTER.registerAll();
    }

    public static void registerItemsForCreativeTab(ResourceKey<CreativeModeTab> tabKey, CreativeModeTab.Output r) {
        if (tabKey == HexCreativeTabs.SCROLLS_KEY)
            generateScrollEntries(r);
        for (var item : ITEM_TABS.getOrDefault(tabKey, Collections.emptyList())) {
            item.register(r);
        }
    }

    private static final Map<ResourceKey<CreativeModeTab>, List<TabEntry>> ITEM_TABS = new LinkedHashMap<>();

    public static final Supplier<Item> AMETHYST_DUST = make("amethyst_dust", () -> new Item(props()));
    public static final Supplier<Item> CHARGED_AMETHYST = make("charged_amethyst", () -> new Item(props()));

    public static final Supplier<Item> QUENCHED_SHARD = make("quenched_allay_shard", () -> new Item(props().rarity(Rarity.UNCOMMON)));

    public static final Supplier<ItemStaff> STAFF_OAK = make("staff/oak", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_SPRUCE = make("staff/spruce", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_BIRCH = make("staff/birch", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_JUNGLE = make("staff/jungle", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_ACACIA = make("staff/acacia", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_DARK_OAK = make("staff/dark_oak", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_CRIMSON = make("staff/crimson", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_WARPED = make("staff/warped", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_MANGROVE = make("staff/mangrove", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_CHERRY = make("staff/cherry", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_BAMBOO = make("staff/bamboo", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_EDIFIED = make("staff/edified", () -> new ItemStaff(unstackable()));
    public static final Supplier<ItemStaff> STAFF_QUENCHED = make("staff/quenched", () -> new ItemStaff(unstackable().rarity(Rarity.UNCOMMON)));
    // mindsplice staffaratus
    public static final Supplier<ItemStaff> STAFF_MINDSPLICE = make("staff/mindsplice", () -> new ItemStaff(unstackable().rarity(Rarity.UNCOMMON)));

    public static final Supplier<ItemLens> SCRYING_LENS = make("lens", () -> new ItemLens(
            IXplatAbstractions.INSTANCE.addEquipSlotFabric(EquipmentSlot.HEAD)
                    .stacksTo(1).attributes(ItemLens.MODIFIERS)));

    public static final Supplier<ItemRobes> ROBES_HOOD = make("robes/hood", () -> new ItemRobes(ArmorItem.Type.HELMET, unstackable()));
    public static final Supplier<ItemRobes> ROBES_TUNIC = make("robes/tunic", () -> new ItemRobes(ArmorItem.Type.CHESTPLATE, unstackable()));
    public static final Supplier<ItemRobes> ROBES_LEGS = make("robes/legs", () -> new ItemRobes(ArmorItem.Type.LEGGINGS, unstackable()));
    public static final Supplier<ItemRobes> ROBES_BOOTS = make("robes/boots", () -> new ItemRobes(ArmorItem.Type.BOOTS, unstackable()));

    public static final Supplier<ItemAbacus> ABACUS = make("abacus", () -> new ItemAbacus(unstackable()));
    public static final Supplier<ItemThoughtKnot> THOUGHT_KNOT = make("thought_knot", () -> new ItemThoughtKnot(unstackable()));
    public static final Supplier<ItemFocus> FOCUS = make("focus", () -> new ItemFocus(unstackable()));
    public static final Supplier<ItemSpellbook> SPELLBOOK = make("spellbook", () -> new ItemSpellbook(unstackable()));

    public static final Supplier<ItemCypher> ANCIENT_CYPHER = make("ancient_cypher", () -> new ItemAncientCypher(unstackable()));
    public static final Supplier<ItemCypher> CYPHER = make("cypher", () -> new ItemCypher(unstackable()));
    public static final Supplier<ItemTrinket> TRINKET = make("trinket", () -> new ItemTrinket(unstackable().rarity(Rarity.UNCOMMON)));
    public static final Supplier<ItemArtifact> ARTIFACT = make("artifact", () -> new ItemArtifact(unstackable().rarity(Rarity.RARE)));

    public static final Supplier<ItemJewelerHammer> JEWELER_HAMMER = make("jeweler_hammer", () ->
            new ItemJewelerHammer(Tiers.IRON, props()
                    .stacksTo(1)
                    .durability(Tiers.DIAMOND.getUses())
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(
                                    modLoc("jeweler_hammer_speed"),
                                    -2.8,
                                    AttributeModifier.Operation.ADD_VALUE
                            ), EquipmentSlotGroup.ANY)
                            .build()
                    )
            )
    );

    public static final Supplier<ItemScroll> SCROLL_SMOL = make("scroll_small", () -> new ItemScroll(props(), 1));
    public static final Supplier<ItemScroll> SCROLL_MEDIUM = make("scroll_medium", () -> new ItemScroll(props(), 2));
    public static final Supplier<ItemScroll> SCROLL_LARGE = make("scroll", () -> new ItemScroll(props(), 3));

    public static final Supplier<ItemSlate> SLATE = make("slate", () -> new ItemSlate(HexBlocks.SLATE.get(), props()));

    public static final Supplier<ItemMediaBattery> BATTERY = make("battery", () ->
            new ItemMediaBattery(unstackable()), null);

    public static final Supplier<ItemStack> BATTERY_DUST_STACK = addToTab(() -> ItemMediaBattery.withMedia(
            new ItemStack(HexItems.BATTERY.get()),
            MediaConstants.DUST_UNIT * 64,
            MediaConstants.DUST_UNIT * 64), HexCreativeTabs.HEX_KEY);
    public static final Supplier<ItemStack> BATTERY_SHARD_STACK = addToTab(() -> ItemMediaBattery.withMedia(
            new ItemStack(HexItems.BATTERY.get()),
            MediaConstants.SHARD_UNIT * 64,
            MediaConstants.SHARD_UNIT * 64), HexCreativeTabs.HEX_KEY);
    public static final Supplier<ItemStack> BATTERY_CRYSTAL_STACK = addToTab(() -> ItemMediaBattery.withMedia(
            new ItemStack(HexItems.BATTERY.get()),
            MediaConstants.CRYSTAL_UNIT * 64,
            MediaConstants.CRYSTAL_UNIT * 64), HexCreativeTabs.HEX_KEY);
    public static final Supplier<ItemStack> BATTERY_QUENCHED_SHARD_STACK = addToTab(() -> ItemMediaBattery.withMedia(
            new ItemStack(HexItems.BATTERY.get()),
            MediaConstants.QUENCHED_SHARD_UNIT * 64,
            MediaConstants.QUENCHED_SHARD_UNIT * 64), HexCreativeTabs.HEX_KEY);
    public static final Supplier<ItemStack> BATTERY_QUENCHED_BLOCK_STACK = addToTab(() -> ItemMediaBattery.withMedia(
            new ItemStack(HexItems.BATTERY.get()),
            MediaConstants.QUENCHED_BLOCK_UNIT * 64,
            MediaConstants.QUENCHED_BLOCK_UNIT * 64), HexCreativeTabs.HEX_KEY);

    public static final EnumMap<DyeColor, Supplier<ItemDyePigment>> DYE_PIGMENTS = Util.make(() -> {
        var out = new EnumMap<DyeColor, Supplier<ItemDyePigment>>(DyeColor.class);
        for (var dye : DyeColor.values()) {
            out.put(dye, make("dye_colorizer_" + dye.getName(), () -> new ItemDyePigment(dye, unstackable())));
        }
        return out;
    });
    public static final EnumMap<ItemPridePigment.Type, Supplier<ItemPridePigment>> PRIDE_PIGMENTS = Util.make(() -> {
        var out = new EnumMap<ItemPridePigment.Type, Supplier<ItemPridePigment>>(ItemPridePigment.Type.class);
        for (var politicsInMyVidya : ItemPridePigment.Type.values()) {
            out.put(politicsInMyVidya, make("pride_colorizer_" + politicsInMyVidya.getName(), () ->
                    new ItemPridePigment(politicsInMyVidya, unstackable())));
        }
        return out;
    });

    public static final Supplier<Item> UUID_PIGMENT = make("uuid_colorizer", () -> new ItemUUIDPigment(unstackable()));
    public static final Supplier<Item> DEFAULT_PIGMENT = make("default_colorizer", () ->
        new ItemAmethystPigment(unstackable()));
    public static final Supplier<Item> ANCIENT_PIGMENT = make("ancient_colorizer", () ->
        new ItemAmethystAndCopperPigment(unstackable()));

    // BUFF SANDVICH
    public static final Supplier<Item> SUBMARINE_SANDWICH = make("sub_sandwich", () ->
            new Item(props().food(new FoodProperties.Builder().nutrition(14).saturationModifier(1.2f).build())));

    public static final Supplier<ItemLoreFragment> LORE_FRAGMENT = make("lore_fragment", () ->
            new ItemLoreFragment(unstackable()
                    .rarity(Rarity.RARE)));

    public static final Supplier<ItemCreativeUnlocker> CREATIVE_UNLOCKER = make("creative_unlocker", () ->
            new ItemCreativeUnlocker(unstackable()
                    .rarity(Rarity.EPIC)
                    .food(new FoodProperties.Builder().nutrition(20).saturationModifier(1f).alwaysEdible().build())));

    //

    public static Item.Properties props() {
        return new Item.Properties();
    }

    public static Item.Properties unstackable() {
        return props().stacksTo(1);
    }

    private static void generateScrollEntries(CreativeModeTab.Output r) {
        var keyList = new ArrayList<ResourceKey<ActionRegistryEntry>>();
        Registry<ActionRegistryEntry> regi = IXplatAbstractions.INSTANCE.getActionRegistry();
        for (var key : regi.registryKeySet())
            if (HexUtils.isOfTag(regi, key, HexTags.Actions.PER_WORLD_PATTERN))
                keyList.add(key);
        keyList.sort(Comparator.comparing(ResourceKey::location));
        for (var key : keyList) {
            r.accept(ItemScroll.withPerWorldPattern(
                    new ItemStack(HexItems.SCROLL_LARGE.get()),
                    key
            ));
        }
    }

    private static <T extends Item> Supplier<T> make(String id, Supplier<T> itemSupplier, @Nullable ResourceKey<CreativeModeTab> tabKey) {
        Supplier<T> supplier = REGISTER.register(id, itemSupplier);
        if (tabKey != null) {
            ITEM_TABS.computeIfAbsent(tabKey, t -> new ArrayList<>()).add(new TabEntry.ItemEntry(supplier::get));
        }
        return supplier;
    }

    private static <T extends Item> Supplier<T> make(String id, Supplier<T> itemSupplier) {
        return make(id, itemSupplier, HexCreativeTabs.HEX_KEY);
    }

    private static Supplier<ItemStack> addToTab(Supplier<ItemStack> stack, ResourceKey<CreativeModeTab> tabKey) {
        var memoised = Suppliers.memoize(stack::get);
        ITEM_TABS.computeIfAbsent(tabKey, t -> new ArrayList<>()).add(new TabEntry.StackEntry(memoised));
        return memoised;
    }

    private static abstract class TabEntry {
        abstract void register(CreativeModeTab.Output r);

        static class ItemEntry extends TabEntry {
            private final Supplier<Item> item;

            ItemEntry(Supplier<Item> item) {
                this.item = item;
            }

            @Override
            void register(CreativeModeTab.Output r) {
                r.accept(item.get());
            }
        }

        static class StackEntry extends TabEntry {
            private final Supplier<ItemStack> stack;

            StackEntry(Supplier<ItemStack> stack) {
                this.stack = stack;
            }

            @Override
            void register(CreativeModeTab.Output r) {
                r.accept(stack.get());
            }
        }
    }
}
