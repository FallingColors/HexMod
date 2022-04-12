package at.petrak.hexcasting.api.item;

import net.minecraft.world.item.ItemStack;

/**
 * Don't use this interface's methods directly. Instead, use an IManaReservoir capability.
 */
public interface ManaHolderItem {
	int getMana(ItemStack stack);
	int getMaxMana(ItemStack stack);
	void setMana(ItemStack stack, int mana);

	boolean manaProvider(ItemStack stack);

	default float getManaFullness(ItemStack stack) {
		int max = getMaxMana(stack);
		if (max == 0)
			return 0;
		return (float) getMana(stack) / (float) max;
	}

	default int withdrawMana(ItemStack stack, int cost, boolean simulate) {
		var manaHere = getMana(stack);
		if (cost < 0)
			cost = manaHere;
		if (!simulate) {
			var manaLeft = manaHere - cost;
			setMana(stack, manaLeft);
		}
		return Math.min(cost, manaHere);
	}
}
