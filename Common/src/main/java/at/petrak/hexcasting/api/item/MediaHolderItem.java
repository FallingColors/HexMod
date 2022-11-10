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
        var mediaHere = getMedia(stack);
        if (cost < 0) {
            cost = mediaHere;
        }
        if (!simulate) {
            var mediaLeft = mediaHere - cost;
            setMedia(stack, mediaLeft);
        }
        return Math.min(cost, mediaHere);
    }

    default int insertMedia(ItemStack stack, int amount, boolean simulate) {
        var mediaHere = getMedia(stack);
        int emptySpace = getMaxMedia(stack) - mediaHere;
        if (emptySpace <= 0) {
            return 0;
        }
        if (amount < 0) {
            amount = emptySpace;
        }

        int inserting = Math.min(amount, emptySpace);

        if (!simulate) {
            var newMedia = mediaHere + inserting;
            setMedia(stack, newMedia);
        }
        return inserting;
    }
}
