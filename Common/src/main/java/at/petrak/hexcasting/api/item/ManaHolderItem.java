package at.petrak.hexcasting.api.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * This interface should not be accessed direc
 */
@ApiStatus.OverrideOnly
public interface ManaHolderItem {
    int getMana(ItemStack stack);

    int getMaxMana(ItemStack stack);

    void setMana(ItemStack stack, int mana);

    boolean manaProvider(ItemStack stack);

    boolean canRecharge(ItemStack stack);

    default float getManaFullness(ItemStack stack) {
        int max = getMaxMana(stack);
		if (max == 0) {
			return 0;
		}
        return (float) getMana(stack) / (float) max;
    }

    default int withdrawMana(ItemStack stack, int cost, boolean simulate) {
        var manaHere = getMana(stack);
		if (cost < 0) {
			cost = manaHere;
		}
        if (!simulate) {
            var manaLeft = manaHere - cost;
            setMana(stack, manaLeft);
        }
        return Math.min(cost, manaHere);
    }

	default int insertMana(ItemStack stack, int amount, boolean simulate) {
		var manaHere = getMana(stack);
		int emptySpace = getMaxMana(stack) - manaHere;
		if (emptySpace <= 0) {
			return 0;
		}
		if (amount < 0) {
			amount = emptySpace;
		}

		int inserting = Math.min(amount, emptySpace);

		if (!simulate) {
			var newMana = manaHere + inserting;
			setMana(stack, newMana);
		}
		return inserting;
	}
}
