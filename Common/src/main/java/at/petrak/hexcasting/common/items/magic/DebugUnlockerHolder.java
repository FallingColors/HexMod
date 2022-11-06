package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.addldata.ManaHolder;
import net.minecraft.world.item.ItemStack;

public record DebugUnlockerHolder(ItemStack creativeUnlocker) implements ManaHolder {
	@Override
	public int getMana() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getMaxMana() {
		return Integer.MAX_VALUE - 1;
	}

	@Override
	public void setMana(int mana) {
		// NO-OP
	}

	@Override
	public boolean canRecharge() {
		return true;
	}

	@Override
	public boolean canProvide() {
		return true;
	}

	@Override
	public int getConsumptionPriority() {
		return 1000;
	}

	@Override
	public boolean canConstructBattery() {
		return false;
	}

	@Override
	public int withdrawMana(int cost, boolean simulate) {
		ItemCreativeUnlocker.addToIntArray(creativeUnlocker, ItemCreativeUnlocker.TAG_EXTRACTIONS, cost);

		return cost < 0 ? getMana() : cost;
	}

	@Override
	public int insertMana(int amount, boolean simulate) {
		ItemCreativeUnlocker.addToIntArray(creativeUnlocker, ItemCreativeUnlocker.TAG_INSERTIONS, amount);

		return amount;
	}
}
