package at.petrak.hexcasting.api.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Items which can store Media can implement this interface.
 * <p>
 * On both the Forge and Fabric sides, the registry will be scanned for all items which implement this interface,
 * and the appropriate cap/CC will be attached.
 */
@ApiStatus.OverrideOnly
public interface MediaHolderItem {
    int getMedia(ItemStack stack);

    int getMaxMedia(ItemStack stack);

    void setMedia(ItemStack stack, int media);

    boolean canProvideMedia(ItemStack stack);

    boolean canRecharge(ItemStack stack);

    default float getMediaFullness(ItemStack stack) {
        int max = getMaxMedia(stack);
        if (max == 0) {
            return 0;
        }
        return (float) getMedia(stack) / (float) max;
    }

    default int withdrawMedia(ItemStack stack, int cost, boolean simulate) {
        var manaHere = getMedia(stack);
        if (cost < 0) {
            cost = manaHere;
        }
        if (!simulate) {
            var manaLeft = manaHere - cost;
            setMedia(stack, manaLeft);
        }
        return Math.min(cost, manaHere);
    }

    default int insertMedia(ItemStack stack, int amount, boolean simulate) {
        var manaHere = getMedia(stack);
        int emptySpace = getMaxMedia(stack) - manaHere;
        if (emptySpace <= 0) {
            return 0;
        }
        if (amount < 0) {
            amount = emptySpace;
        }

        int inserting = Math.min(amount, emptySpace);

        if (!simulate) {
            var newMana = manaHere + inserting;
            setMedia(stack, newMana);
        }
        return inserting;
    }
}
