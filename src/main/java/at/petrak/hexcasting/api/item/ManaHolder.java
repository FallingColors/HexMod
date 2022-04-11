package at.petrak.hexcasting.api.item;

import net.minecraft.world.item.ItemStack;

public interface ManaHolder {
	int getMana(ItemStack stack);
	int getMaxMana(ItemStack stack);
	void setMana(ItemStack stack, int mana);

	int getConsumptionPriority(ItemStack stack);

	boolean canConstructBattery(ItemStack stack);

	default float getManaFullness(ItemStack stack) {
		int max = getMaxMana(stack);
		if (max == 0)
			return 0;
		return (float) getMana(stack) / (float) max;
	}

	default int withdrawMana(ItemStack stack, int cost) {
		var manaHere = getMana(stack);
		var manaLeft = manaHere - cost;
		setMana(stack, manaLeft);
		return Math.min(cost, manaHere);
	}
}
