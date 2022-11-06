package at.petrak.hexcasting.interop.utils;

import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.common.items.magic.ItemManaBattery;
import at.petrak.hexcasting.common.lib.HexItems;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class PhialRecipeStackBuilder {
	private static ItemStack makeBattery(int unit, int size) {
		return ItemManaBattery.withMana(new ItemStack(HexItems.BATTERY), unit * size, unit * size);
	}

	public static Pair<List<ItemStack>, List<ItemStack>> createStacks() {
		List<ItemStack> inputItems = Lists.newArrayList();
		List<ItemStack> outputItems = Lists.newArrayList();

		int dust = HexConfig.common().dustManaAmount();
		int shard = HexConfig.common().shardManaAmount();
		int charged = HexConfig.common().chargedCrystalManaAmount();

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

		return new Pair<>(inputItems, outputItems);
	}

	public static boolean shouldAddRecipe() {
		return HexConfig.common().dustManaAmount() > 0 ||
			HexConfig.common().shardManaAmount() > 0 ||
			HexConfig.common().chargedCrystalManaAmount() > 0;
	}
}
