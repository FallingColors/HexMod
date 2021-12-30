package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.lib.LibItemNames;
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
            var tag = new CompoundTag();
            tag.putInt(ItemWand.TAG_MANA, 1000);
            tag.putInt(ItemWand.TAG_MAX_MANA, 1000);
            var stack = new ItemStack(WAND::get);
            stack.setTag(tag);
            items.add(stack);

            super.fillItemList(items);
        }
    };

    public static final RegistryObject<Item> WAND = ITEMS.register(LibItemNames.WAND,
            () -> new ItemWand(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FOCUS = ITEMS.register(LibItemNames.FOCUS,
            () -> new ItemFocus(props()));
    public static final RegistryObject<Item> SPELLBOOK = ITEMS.register(LibItemNames.SPELLBOOK,
            () -> new ItemSpellbook(unstackable()));

    public static Item.Properties props() {
        return new Item.Properties().tab(TAB);
    }

    public static Item.Properties unstackable() {
        return props().stacksTo(1);
    }
}
