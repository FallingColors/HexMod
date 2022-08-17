package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.addldata.ManaHolder;
import at.petrak.hexcasting.api.utils.NBTHelper;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

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
		return false;
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
		int[] arr = NBTHelper.getIntArray(creativeUnlocker, ItemCreativeUnlocker.TAG_EXTRACTIONS);
		if (arr == null) {
			arr = new int[0];
		}
		int[] newArr = Arrays.copyOf(arr, arr.length + 1);
		newArr[newArr.length - 1] = cost;
		NBTHelper.putIntArray(creativeUnlocker, ItemCreativeUnlocker.TAG_EXTRACTIONS, newArr);

		return cost < 0 ? 1 : cost;
	}
}
