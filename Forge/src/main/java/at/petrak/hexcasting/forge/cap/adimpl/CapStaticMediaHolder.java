package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * Things that always hold a constant amount of media, like amethyst
 */
public record CapStaticMediaHolder(Supplier<Integer> baseWorth,
                                   int consumptionPriority,
                                   ItemStack stack) implements ADMediaHolder {
    @Override
    public int getMedia() {
        return baseWorth.get() * stack.getCount();
    }

    @Override
    public int getMaxMedia() {
        return getMedia();
    }

    @Override
    public void setMedia(int media) {
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
        return consumptionPriority;
    }

    @Override
    public boolean canConstructBattery() {
        return true;
    }

    @Override
    public int withdrawMedia(int cost, boolean simulate) {
        int worth = baseWorth.get();
        if (cost < 0) {
            cost = worth * stack.getCount();
        }
        double itemsRequired = cost / (double) worth;
        int itemsUsed = Math.min((int) Math.ceil(itemsRequired), stack.getCount());
        if (!simulate) {
            stack.shrink(itemsUsed);
        }
        return itemsUsed * worth;
    }
}
