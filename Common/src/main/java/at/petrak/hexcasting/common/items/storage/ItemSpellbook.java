package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static at.petrak.hexcasting.common.items.storage.ItemFocus.NUM_VARIANTS;

public class ItemSpellbook extends Item implements IotaHolderItem, VariantItem {
    public static final int MAX_PAGES = 64;

    public ItemSpellbook(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
        boolean sealed = isSealed(stack);
        boolean empty = false;
        if (stack.has(HexDataComponents.SELECTED_PAGE)) {
            var pageIdx = stack.get(HexDataComponents.SELECTED_PAGE);
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
            boolean overridden = stack.has(HexDataComponents.VISUAL_OVERRIDE);
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

        super.appendHoverText(stack, context, tooltip, isAdvanced);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity pEntity, int pSlotId, boolean pIsSelected) {
        int index = getPage(stack, 0);
        stack.set(HexDataComponents.SELECTED_PAGE, index);

        int shiftedIdx = Math.max(1, index);
        String nameKey = String.valueOf(shiftedIdx);

        var customName = stack.get(DataComponents.CUSTOM_NAME);
        var savedNames = stack.get(HexDataComponents.PAGE_NAMES);

        if(customName != null) {
            if(savedNames != null) {
                if(!savedNames.containsKey(nameKey) || !savedNames.get(nameKey).equals(customName)) {
                    var mutNames = new HashMap<>(savedNames);
                    mutNames.put(nameKey, customName);
                    stack.set(HexDataComponents.PAGE_NAMES, mutNames);
                }
            } else {
                var mutNames = new HashMap<String, Component>();
                mutNames.put(nameKey, customName);
                stack.set(HexDataComponents.PAGE_NAMES, mutNames);
            }
        } else if(savedNames != null) {
            var mutNames = new HashMap<>(savedNames);
            mutNames.remove(nameKey);
            if(mutNames.isEmpty()) {
                stack.remove(HexDataComponents.PAGE_NAMES);
            } else {
                stack.set(HexDataComponents.PAGE_NAMES, mutNames);
            }
        }
    }

    public static boolean arePagesEmpty(ItemStack stack) {
        var pages = stack.get(HexDataComponents.PAGES);
        return pages == null || pages.isEmpty();
    }

    @Override
    public @Nullable Iota readIota(ItemStack stack) {
        int idx = getPage(stack, 1);
        var key = String.valueOf(idx);
        var pages = stack.get(HexDataComponents.PAGES);
        if (pages != null && pages.containsKey(key)) {
            return pages.get(key);
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

        var pages = stack.get(HexDataComponents.PAGES);

        if (pages != null) {
            var pagesMut = new HashMap<>(pages);

            if (datum == null) {
                pagesMut.remove(key);
                var seals = stack.get(HexDataComponents.PAGE_SEALS);
                if(seals != null) {
                    var sealsMut = new HashMap<>(seals);

                    sealsMut.remove(key);

                    if(sealsMut.isEmpty()) {
                        stack.remove(HexDataComponents.PAGE_SEALS);
                    } else {
                        stack.set(HexDataComponents.PAGE_SEALS, sealsMut);
                    }
                }
            } else {
                pagesMut.put(key, datum);
            }

            if (pagesMut.isEmpty()) {
                stack.remove(HexDataComponents.PAGES);
            } else {
                stack.set(HexDataComponents.PAGES, pagesMut);
            }
        } else if (datum != null) {
            var map = new HashMap<String, Iota>();
            map.put(key, datum);
            stack.set(HexDataComponents.PAGES, map);
        } else {
            var seals = stack.get(HexDataComponents.PAGE_SEALS);
            if(seals != null) {
                var sealsMut = new HashMap<>(seals);
                sealsMut.remove(key);

                if(sealsMut.isEmpty()) {
                    stack.remove(HexDataComponents.PAGE_SEALS);
                } else {
                    stack.set(HexDataComponents.PAGE_SEALS, sealsMut);
                }
            }
        }
    }

    public static int getPage(ItemStack stack, int ifEmpty) {
        if (arePagesEmpty(stack)) {
            return ifEmpty;
        } else if (stack.has(HexDataComponents.SELECTED_PAGE)) {
            var index = stack.get(HexDataComponents.SELECTED_PAGE);
            if(index == null)
                return 1;
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

        var seals = stack.get(HexDataComponents.PAGE_SEALS);

        var sealsMut = seals != null ? new HashMap<>(seals) : new HashMap<String, Boolean>();

        if (!sealed) {
            sealsMut.remove(nameKey);
        } else {
            sealsMut.put(nameKey, true);
        }

        if (sealsMut.isEmpty()) {
            stack.remove(HexDataComponents.PAGE_SEALS);
        } else {
            stack.set(HexDataComponents.PAGE_SEALS, sealsMut);
        }
    }

    public static boolean isSealed(ItemStack stack) {
        int index = getPage(stack, 1);

        String nameKey = String.valueOf(index);
        var seals = stack.get(HexDataComponents.PAGE_SEALS);
        if(seals == null)
            return false;
        var v = seals.get(nameKey);
        return v != null && v;
    }

    public static int highestPage(ItemStack stack) {
        var pages = stack.get(HexDataComponents.PAGES);
        if (pages == null) {
            return 0;
        }
        return pages.keySet().stream().flatMap(s -> {
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
        stack.set(HexDataComponents.SELECTED_PAGE, idx);

        var names = stack.getOrDefault(HexDataComponents.PAGE_NAMES, Collections.<String, Component>emptyMap());
        int shiftedIdx = Math.max(1, idx);
        String nameKey = String.valueOf(shiftedIdx);
        Component name = names.get(nameKey);
        if (name != null) {
            stack.set(DataComponents.CUSTOM_NAME, name);
        } else {
            stack.remove(DataComponents.CUSTOM_NAME);
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
            stack.set(HexDataComponents.VARIANT, clampVariant(variant));
    }
}
