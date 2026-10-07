package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
        if (stack.has(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get())) {
            var pageIdx = stack.get(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get());
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
            boolean overridden = stack.has(HexDataComponents.VISUAL_OVERRIDE.get());
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
        stack.set(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get(), index);

        int shiftedIdx = Math.max(1, index);
        String nameKey = String.valueOf(shiftedIdx);

        var customName = stack.get(DataComponents.CUSTOM_NAME);
        var savedNames = stack.get(HexDataComponents.SPELLBOOK_PAGE_NAMES.get());

        if(customName != null) {
            // the stack has been given a custom name (ie via anvil)
            if(savedNames != null) {
                if(!savedNames.containsKey(nameKey) || !savedNames.get(nameKey).equals(customName)) {
                    // if this page doesn' have a name mapping, or it doesn't match, create/update the name mapping
                    var mutNames = new HashMap<>(savedNames);
                    mutNames.put(nameKey, customName);
                    stack.set(HexDataComponents.SPELLBOOK_PAGE_NAMES.get(), mutNames);
                }
            } else {
                var mutNames = new HashMap<String, Component>();
                mutNames.put(nameKey, customName);
                // if the savedNames map doesn't exist at all, create it and map the stack's current name to this page
                stack.set(HexDataComponents.SPELLBOOK_PAGE_NAMES.get(), mutNames);
            }
        } else if(savedNames != null) {
            // the stack does not have a custom name, or it has been removed
            var mutNames = new HashMap<>(savedNames);
            mutNames.remove(nameKey);
            if(mutNames.isEmpty()) {
                stack.remove(HexDataComponents.SPELLBOOK_PAGE_NAMES.get());
            } else {
                stack.set(HexDataComponents.SPELLBOOK_PAGE_NAMES.get(), mutNames);
            }
        }
    }

    public static boolean arePagesEmpty(ItemStack stack) {
        var pages = stack.get(HexDataComponents.SPELLBOOK_PAGES.get());
        return pages == null || pages.isEmpty();
    }

    @Override
    public @Nullable Iota readIota(ItemStack stack) {
        int idx = getPage(stack, 1);
        var key = String.valueOf(idx);
        var pages = stack.get(HexDataComponents.SPELLBOOK_PAGES.get());
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

        var pages = stack.get(HexDataComponents.SPELLBOOK_PAGES.get());

        if (pages != null) {
            // if the pages map exists, modify it accordingly
            var pagesMut = new HashMap<>(pages);

            if (datum == null) {
                pagesMut.remove(key);
                // erasing the current page (needs to unseal as well, if possible)
                var seals = stack.get(HexDataComponents.SPELLBOOK_PAGE_SEALS.get());
                if(seals != null) {
                    var sealsMut = new HashMap<>(seals);

                    sealsMut.remove(key);

                    if(sealsMut.isEmpty()) {
                        stack.remove(HexDataComponents.SPELLBOOK_PAGE_SEALS.get());
                    } else {
                        stack.set(HexDataComponents.SPELLBOOK_PAGE_SEALS.get(), sealsMut);
                    }
                }
            } else {
                pagesMut.put(key, datum);
                // updating the current page
            }

            if (pagesMut.isEmpty()) {
                stack.remove(HexDataComponents.SPELLBOOK_PAGES.get());
            } else {
                stack.set(HexDataComponents.SPELLBOOK_PAGES.get(), pagesMut);
            }
        } else if (datum != null) {
            var map = new HashMap<String, Iota>();
            map.put(key, datum);
            // if the pages map doesn't exist and you're trying to update a page, create the map first
            stack.set(HexDataComponents.SPELLBOOK_PAGES.get(), map);
        } else {
            // if the pages map doesn't exist and you're trying to erase a page, check for a seal to remove
            // this can happen if somebody seals an empty book for some reason
            var seals = stack.get(HexDataComponents.SPELLBOOK_PAGE_SEALS.get());
            if(seals != null) {
                var sealsMut = new HashMap<>(seals);
                sealsMut.remove(key);

                if(sealsMut.isEmpty()) {
                    stack.remove(HexDataComponents.SPELLBOOK_PAGE_SEALS.get());
                } else {
                    stack.set(HexDataComponents.SPELLBOOK_PAGE_SEALS.get(), sealsMut);
                }
            }
        }
    }

    public static int getPage(ItemStack stack, int ifEmpty) {
        if (arePagesEmpty(stack)) {
            return ifEmpty;
        } else if (stack.has(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get())) {
            var index = stack.get(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get());
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

        var seals = stack.get(HexDataComponents.SPELLBOOK_PAGE_SEALS.get());

        var sealsMut = seals != null ? new HashMap<>(seals) : new HashMap<String, Boolean>();

        if (!sealed) {
            sealsMut.remove(nameKey);
        } else {
            sealsMut.put(nameKey, true);
        }

        if (sealsMut.isEmpty()) {
            stack.remove(HexDataComponents.SPELLBOOK_PAGE_SEALS.get());
        } else {
            stack.set(HexDataComponents.SPELLBOOK_PAGE_SEALS.get(), sealsMut);
        }
    }

    public static boolean isSealed(ItemStack stack) {
        int index = getPage(stack, 1);

        String nameKey = String.valueOf(index);
        var seals = stack.get(HexDataComponents.SPELLBOOK_PAGE_SEALS.get());
        if(seals == null)
            return false;
        var v = seals.get(nameKey);
        return v != null && v;
    }

    public static int highestPage(ItemStack stack) {
        var pages = stack.get(HexDataComponents.SPELLBOOK_PAGES.get());
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

    public static int rotatePageIdx(ItemStack stack, boolean increase, Level level) {
        int idx = getPage(stack, 0);
        if (idx != 0) {
            idx += increase ? 1 : -1;
            idx = Math.max(1, idx);
        }
        idx = Mth.clamp(idx, 0, MAX_PAGES);
        stack.set(HexDataComponents.SELECTED_SPELLBOOK_PAGE.get(), idx);

        var names = stack.getOrDefault(HexDataComponents.SPELLBOOK_PAGE_NAMES.get(), Collections.<String, Component>emptyMap());
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
            stack.set(HexDataComponents.ITEM_VARIANT.get(), clampVariant(variant));
    }
}
