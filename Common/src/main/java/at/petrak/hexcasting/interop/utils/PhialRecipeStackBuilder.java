package at.petrak.hexcasting.interop.utils;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.common.items.magic.ItemMediaBattery;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class PhialRecipeStackBuilder {
    private static ItemStack makeBattery(long unit, int size) {
        return ItemMediaBattery.withMedia(new ItemStack(HexItems.BATTERY), unit * size, unit * size);
    }

    public static Pair<List<ItemStack>, List<ItemStack>> createStacks() {
        List<ItemStack> inputItems = Lists.newArrayList();
        List<ItemStack> outputItems = Lists.newArrayList();

        long dust = HexConfig.common().dustMediaAmount();
        long shard = HexConfig.common().shardMediaAmount();
        long charged = HexConfig.common().chargedCrystalMediaAmount();
        long quenchedShard = MediaConstants.QUENCHED_SHARD_UNIT;
        long quenchedBlock = MediaConstants.QUENCHED_BLOCK_UNIT;


        if (dust > 0) {
            inputItems.add(new ItemStack(HexItems.AMETHYST_DUST, 1));
            outputItems.add(makeBattery(dust, 1));
            inputItems.add(new ItemStack(HexItems.AMETHYST_DUST, 64));
            outputItems.add(makeBattery(dust, 64));
        }

        if (shard > 0) {
            inputItems.add(new ItemStack(Items.AMETHYST_SHARD, 1));
            outputItems.add(makeBattery(shard, 1));
            inputItems.add(new ItemStack(Items.AMETHYST_SHARD, 64));
            outputItems.add(makeBattery(shard, 64));
        }

        if (charged > 0) {
            inputItems.add(new ItemStack(HexItems.CHARGED_AMETHYST, 1));
            outputItems.add(makeBattery(charged, 1));
            inputItems.add(new ItemStack(HexItems.CHARGED_AMETHYST, 64));
            outputItems.add(makeBattery(charged, 64));
        }

        inputItems.add(new ItemStack(HexItems.QUENCHED_SHARD, 1));
        outputItems.add(makeBattery(quenchedShard, 1));
        inputItems.add(new ItemStack(HexItems.QUENCHED_SHARD, 64));
        outputItems.add(makeBattery(quenchedShard, 64));

        inputItems.add(new ItemStack(HexBlocks.QUENCHED_ALLAY, 1));
        outputItems.add(makeBattery(quenchedBlock, 1));
        inputItems.add(new ItemStack(HexBlocks.QUENCHED_ALLAY, 64));
        outputItems.add(makeBattery(quenchedBlock, 64));

        return new Pair<>(inputItems, outputItems);
    }

    public static boolean shouldAddRecipe() {
        return HexConfig.common().dustMediaAmount() > 0 ||
            HexConfig.common().shardMediaAmount() > 0 ||
            HexConfig.common().chargedCrystalMediaAmount() > 0;
    }
}
