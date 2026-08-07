package at.petrak.hexcasting.interop.utils;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.MediaHelper;
import at.petrak.hexcasting.common.items.magic.ItemMediaBattery;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import net.minecraft.world.level.ItemLike;

public class PhialRecipeStackBuilder {
    private static ItemStack makeBattery(long media) {
        return ItemMediaBattery.withMedia(new ItemStack(HexItems.BATTERY.get()), media, media);
    }

    private static final List<Item> FALLBACK_BATTERY_LIST = List.of(
            HexItems.AMETHYST_DUST.get(),
            Items.AMETHYST_SHARD,
            HexItems.CHARGED_AMETHYST.get(),
            HexItems.QUENCHED_SHARD.get(),
            HexBlocks.QUENCHED_ALLAY.get().asItem()
    );

    private static void addPhialRecipe(Item of, int count, List<ItemStack> from, List<ItemStack> to) {
        ItemStack stack = new ItemStack(of, count);
        long resultMedia = MediaHelper.extractMedia(stack, -1L, true, true);

        from.add(stack);
        to.add(makeBattery(resultMedia));
    }

    public static Pair<List<ItemStack>, List<ItemStack>> createStacks() {
        List<ItemStack> inputItems = Lists.newArrayList();
        List<ItemStack> outputItems = Lists.newArrayList();

        List<Item> toUse = BuiltInRegistries.ITEM.getTag(HexTags.Items.PHIAL_RAW_INGREDIENTS)
                .map(h -> h.stream().map(Holder::value).toList())
                .orElse(FALLBACK_BATTERY_LIST);

        for(Item i : toUse) {
            addPhialRecipe(i, 1, inputItems, outputItems);
            if(i.getDefaultMaxStackSize() > 1) {
                addPhialRecipe(i, i.getDefaultMaxStackSize(), inputItems, outputItems);
            }
        }

        return new Pair<>(inputItems, outputItems);
    }

    public static boolean shouldAddRecipe() {
        return HexConfig.common().dustMediaAmount() > 0 ||
            HexConfig.common().shardMediaAmount() > 0 ||
            HexConfig.common().chargedCrystalMediaAmount() > 0;
    }
}
