package at.petrak.hexcasting.api.item;

import net.minecraft.world.item.ItemStack;

/**
 * Items which can store Media can implement this interface.
 * <p>
 * On both the Forge and Fabric sides, the registry will be scanned for all items which implement this interface,
 * and the appropriate cap/CC will be attached.
 */
public interface MediaHolderItem {
    int getMedia(ItemStack stack);

    int getMaxMedia(ItemStack stack);

    void setMedia(ItemStack stack, int media);

    boolean canProvideMedia(ItemStack stack);

    boolean canRecharge(ItemStack stack);

    default float getManaFullness(ItemStack stack) {
        int max = getMaxMedia(stack);
        if (max == 0) {
            return 0;
        }
        return (float) getMedia(stack) / (float) max;
    }

    default int withdrawMana(ItemStack stack, int cost, boolean simulate) {
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
}
