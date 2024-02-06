package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

import static at.petrak.hexcasting.common.items.storage.ItemFocus.NUM_VARIANTS;

public class ItemSpellbook extends Item implements IotaHolderItem, VariantItem {
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

    // this stores which variant of the spellbook should be rendered
    public static final String TAG_VARIANT = "variant";

    public static final int MAX_PAGES = 64;

    public ItemSpellbook(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
        TooltipFlag isAdvanced) {
        boolean sealed = isSealed(stack);
        boolean empty = false;
        if (NBTHelper.hasNumber(stack, TAG_SELECTED_PAGE)) {
            var pageIdx = NBTHelper.getInt(stack, TAG_SELECTED_PAGE);
            int highest = highestPage(stack);
            if (highest != 0) {
                if (sealed) {
                    tooltip.add(Component.translatable("hexcasting.tooltip.spellbook.page.sealed",
                            Component.literal(String.valueOf(pageIdx)).withStyle(ChatFormatting.WHITE),
                            Component.literal(String.valueOf(highest)).withStyle(ChatFormatting.WHITE),
                            Component.translatable("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(Component.translatable("hexcasting.tooltip.spellbook.page",
                            Component.literal(String.valueOf(pageIdx)).withStyle(ChatFormatting.WHITE),
                            Component.literal(String.valueOf(highest)).withStyle(ChatFormatting.WHITE))
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
                    tooltip.add(Component.translatable("hexcasting.tooltip.spellbook.sealed").withStyle(
                        ChatFormatting.GOLD));
                } else {
                    tooltip.add(Component.translatable("hexcasting.tooltip.spellbook.empty.sealed",
                            Component.translatable("hexcasting.tooltip.spellbook.sealed").withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.GRAY));
                }
            } else if (!overridden) {
                tooltip.add(
                    Component.translatable("hexcasting.tooltip.spellbook.empty").withStyle(ChatFormatting.GRAY));
            }
        }

        IotaHolderItem.appendHoverText(this, stack, tooltip, isAdvanced);

        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        int index = getPage(stack, 0);
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

    public static boolean arePagesEmpty(ItemStack stack) {
        CompoundTag tag = NBTHelper.getCompound(stack, TAG_PAGES);
        return tag == null || tag.isEmpty();
    }

    @Override
    public @Nullable
    CompoundTag readIotaTag(ItemStack stack) {
        int idx = getPage(stack, 1);
        var key = String.valueOf(idx);
        var tag = NBTHelper.getCompound(stack, TAG_PAGES);
        if (tag != null && tag.contains(key, Tag.TAG_COMPOUND)) {
            return tag.getCompound(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return !isSealed(stack);
    }

    @Override
    public boolean canWrite(ItemStack stack, Iota datum) {
        return datum == null || !isSealed(stack);
    }

    @Override
    public void writeDatum(ItemStack stack, Iota datum) {
        if (datum != null && isSealed(stack)) {
            return;
        }

        int idx = getPage(stack, 1);
        var key = String.valueOf(idx);
        CompoundTag pages = NBTHelper.getCompound(stack, TAG_PAGES);
        if (pages != null) {
            if (datum == null) {
                pages.remove(key);
                NBTHelper.remove(NBTHelper.getCompound(stack, TAG_SEALED), key);
            } else {
                pages.put(key, IotaType.serialize(datum));
            }

            if (pages.isEmpty()) {
                NBTHelper.remove(stack, TAG_PAGES);
            }
        } else if (datum != null) {
            NBTHelper.getOrCreateCompound(stack, TAG_PAGES).put(key, IotaType.serialize(datum));
        } else {
            NBTHelper.remove(NBTHelper.getCompound(stack, TAG_SEALED), key);
        }
    }

    public static int getPage(ItemStack stack, int ifEmpty) {
        if (arePagesEmpty(stack)) {
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

    public static void setSealed(ItemStack stack, boolean sealed) {
        int index = getPage(stack, 1);

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

    public static boolean isSealed(ItemStack stack) {
        int index = getPage(stack, 1);

        String nameKey = String.valueOf(index);
        CompoundTag names = NBTHelper.getCompound(stack, TAG_SEALED);
        return NBTHelper.getBoolean(names, nameKey);
    }

    public static int highestPage(ItemStack stack) {
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

    public static int rotatePageIdx(ItemStack stack, boolean increase) {
        int idx = getPage(stack, 0);
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

    @Override
    public int numVariants() {
        return NUM_VARIANTS;
    }

    @Override
    public void setVariant(ItemStack stack, int variant) {
        if (!isSealed(stack))
            NBTHelper.putInt(stack, TAG_VARIANT, clampVariant(variant));
    }
}
