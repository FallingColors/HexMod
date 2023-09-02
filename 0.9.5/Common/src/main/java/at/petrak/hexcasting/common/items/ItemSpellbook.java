package at.petrak.hexcasting.common.items;

import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.Widget;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class ItemSpellbook extends Item implements DataHolderItem {
    public static String TAG_SELECTED_PAGE = "page_idx";
    // this is a CompoundTag of string numerical keys to SpellData
    // it is 1-indexed, so that 0/0 can be the special case of "it is empty"
    public static String TAG_PAGES = "pages";

    // this stores the names of pages, to be restored when you scroll
    // it is 1-indexed, and the 0-case for TAG_PAGES will be treated as 1
    public static String TAG_PAGE_NAMES = "page_names";

    // this stores the sealed status of each page, to be restored when you scroll
    // it is 1-indexed, and the 0-case for TAG_PAGES will be treated as 1
    public static String TAG_SEALED = "sealed_pages";

    public static final int MAX_PAGES = 64;

    public ItemSpellbook(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
        TooltipFlag isAdvanced) {
        boolean sealed = IsSealed(stack);
        boolean empty = false;
        if (NBTHelper.hasNumber(stack, TAG_SELECTED_PAGE)) {
            var pageIdx = NBTHelper.getInt(stack, TAG_SELECTED_PAGE);
            int highest = HighestPage(stack);
            if (highest != 0) {
                if (sealed) {
                    tooltip.add(new TranslatableComponent("hexcasting.tooltip.spellbook.page.sealed",
                        new TextComponent(String.valueOf(pageIdx)).withStyle(ChatFormatting.WHITE),
                        new TextComponent(String.valueOf(highest)).withStyle(ChatFormatting.WHITE),
                        new TranslatableComponent("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(new TranslatableComponent("hexcasting.tooltip.spellbook.page",
                        new TextComponent(String.valueOf(pageIdx)).withStyle(ChatFormatting.WHITE),
                        new TextComponent(String.valueOf(highest)).withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.GRAY));
                }
            } else {
                empty = true;
            }
        } else {
            empty = true;
        }

        if (empty) {
            boolean overridden = NBTHelper.hasString(stack, TAG_OVERRIDE_VISUALLY);
            if (sealed) {
                if (overridden) {
                    tooltip.add(new TranslatableComponent("hexcasting.tooltip.spellbook.sealed").withStyle(
                        ChatFormatting.GOLD));
                } else {
                    tooltip.add(new TranslatableComponent("hexcasting.tooltip.spellbook.empty.sealed",
                        new TranslatableComponent("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.GRAY));
                }
            } else if (!overridden) {
                tooltip.add(
                    new TranslatableComponent("hexcasting.tooltip.spellbook.empty").withStyle(ChatFormatting.GRAY));
            }
        }

        DataHolderItem.appendHoverText(this, stack, tooltip, isAdvanced);

        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        int index = GetPage(stack, 0);
        NBTHelper.putInt(stack, TAG_SELECTED_PAGE, index);

        int shiftedIdx = Math.max(1, index);
        String nameKey = String.valueOf(shiftedIdx);
        CompoundTag names = NBTHelper.getOrCreateCompound(stack, TAG_PAGE_NAMES);
        if (stack.hasCustomHoverName()) {
            names.putString(nameKey, Component.Serializer.toJson(stack.getHoverName()));
        } else {
            names.remove(nameKey);
        }
    }

    public static boolean ArePagesEmpty(ItemStack stack) {
        CompoundTag tag = NBTHelper.getCompound(stack, TAG_PAGES);
        return tag == null || tag.isEmpty();
    }

    @Override
    public @Nullable CompoundTag readDatumTag(ItemStack stack) {
        int idx = GetPage(stack, 1);
        var key = String.valueOf(idx);
        var tag = NBTHelper.getCompound(stack, TAG_PAGES);
        if (tag != null && tag.contains(key, Tag.TAG_COMPOUND)) {
            return tag.getCompound(key);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable SpellDatum<?> emptyDatum(ItemStack stack) {
        return SpellDatum.make(Widget.NULL);
    }

    @Override
    public boolean canWrite(ItemStack stack, SpellDatum<?> datum) {
        return datum == null || !IsSealed(stack);
    }

    @Override
    public void writeDatum(ItemStack stack, SpellDatum<?> datum) {
        if (datum != null && IsSealed(stack)) {
            return;
        }

        int idx = GetPage(stack, 1);
        var key = String.valueOf(idx);
        CompoundTag pages = NBTHelper.getCompound(stack, TAG_PAGES);
        if (pages != null) {
            if (datum == null) {
                pages.remove(key);
                NBTHelper.remove(NBTHelper.getCompound(stack, TAG_SEALED), key);
            } else {
                pages.put(key, datum.serializeToNBT());
            }

            if (pages.isEmpty()) {
                NBTHelper.remove(stack, TAG_PAGES);
            }
        } else if (datum != null) {
            NBTHelper.getOrCreateCompound(stack, TAG_PAGES).put(key, datum.serializeToNBT());
        } else {
            NBTHelper.remove(NBTHelper.getCompound(stack, TAG_SEALED), key);
        }
    }

    public static int GetPage(ItemStack stack, int ifEmpty) {
        if (ArePagesEmpty(stack)) {
            return ifEmpty;
        } else if (NBTHelper.hasNumber(stack, TAG_SELECTED_PAGE)) {
            int index = NBTHelper.getInt(stack, TAG_SELECTED_PAGE);
            if (index == 0) {
                index = 1;
            }
            return index;
        } else {
            return 1;
        }
    }

    public static void SetSealed(ItemStack stack, boolean sealed) {
        int index = GetPage(stack, 1);

        String nameKey = String.valueOf(index);
        CompoundTag names = NBTHelper.getOrCreateCompound(stack, TAG_SEALED);

        if (!sealed) {
            names.remove(nameKey);
        } else {
            names.putBoolean(nameKey, true);
        }

        if (names.isEmpty()) {
            NBTHelper.remove(stack, TAG_SEALED);
        } else {
            NBTHelper.putCompound(stack, TAG_SEALED, names);
        }

    }

    public static boolean IsSealed(ItemStack stack) {
        int index = GetPage(stack, 1);

        String nameKey = String.valueOf(index);
        CompoundTag names = NBTHelper.getCompound(stack, TAG_SEALED);
        return NBTHelper.getBoolean(names, nameKey);
    }

    public static int HighestPage(ItemStack stack) {
        CompoundTag tag = NBTHelper.getCompound(stack, TAG_PAGES);
        if (tag == null) {
            return 0;
        }
        return tag.getAllKeys().stream().flatMap(s -> {
            try {
                return Stream.of(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return Stream.empty();
            }
        }).max(Integer::compare).orElse(0);
    }

    public static int RotatePageIdx(ItemStack stack, boolean increase) {
        int idx = GetPage(stack, 0);
        if (idx != 0) {
            idx += increase ? 1 : -1;
            idx = Math.max(1, idx);
        }
        idx = Mth.clamp(idx, 0, MAX_PAGES);
        NBTHelper.putInt(stack, TAG_SELECTED_PAGE, idx);

        CompoundTag names = NBTHelper.getCompound(stack, TAG_PAGE_NAMES);
        int shiftedIdx = Math.max(1, idx);
        String nameKey = String.valueOf(shiftedIdx);
        String name = NBTHelper.getString(names, nameKey);
        if (name != null) {
            stack.setHoverName(Component.Serializer.fromJson(name));
        } else {
            stack.resetHoverName();
        }

        return idx;
    }
}
