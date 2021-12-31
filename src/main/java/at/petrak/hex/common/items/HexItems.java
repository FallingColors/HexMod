package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.lib.LibItemNames;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
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
            // Make the wand spawn with some sensible NBT
            for (Pair<Integer, RegistryObject<Item>> p : new Pair[]{
                    new Pair<>(HexMod.CONFIG.wandMaxMana.get(), WAND),
                    new Pair<>(HexMod.CONFIG.cypherMaxMana.get(), CYPHER),
                    new Pair<>(HexMod.CONFIG.trinketMaxMana.get(), TRINKET),
                    new Pair<>(HexMod.CONFIG.artifactMaxMana.get(), ARTIFACT),
            }) {
                var mana = p.getFirst();
                var stack = new ItemStack(p.getSecond()::get);

                var tag = new CompoundTag();
                tag.putInt(ItemManaHolder.TAG_MANA, mana);
                tag.putInt(ItemWand.TAG_MAX_MANA, mana);
                stack.setTag(tag);
                items.add(stack);
            }

            super.fillItemList(items);
        }
    };

    public static final RegistryObject<Item> WAND = ITEMS.register(LibItemNames.WAND,
            () -> new ItemWand(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FOCUS = ITEMS.register(LibItemNames.FOCUS,
            () -> new ItemFocus(props()));
    public static final RegistryObject<Item> SPELLBOOK = ITEMS.register(LibItemNames.SPELLBOOK,
            () -> new ItemSpellbook(unstackable()));
    public static final RegistryObject<Item> CYPHER = ITEMS.register(LibItemNames.CYPHER,
            () -> new ItemCypher(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TRINKET = ITEMS.register(LibItemNames.TRINKET,
            () -> new ItemTrinket(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ARTIFACT = ITEMS.register(LibItemNames.ARTIFACT,
            () -> new ItemArtifact(new Item.Properties().stacksTo(1)));

    public static Item.Properties props() {
        return new Item.Properties().tab(TAB);
    }

    public static Item.Properties unstackable() {
        return props().stacksTo(1);
    }
}
