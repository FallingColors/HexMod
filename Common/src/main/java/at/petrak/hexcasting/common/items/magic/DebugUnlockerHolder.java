package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import net.minecraft.world.item.ItemStack;

public record DebugUnlockerHolder(ItemStack creativeUnlocker) implements ADMediaHolder {
    @Override
    public long getMedia() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getMaxMedia() {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public void setMedia(long media) {
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
    public long withdrawMedia(long cost, boolean simulate) {
        ItemCreativeUnlocker.addToLongArray(creativeUnlocker, ItemCreativeUnlocker.TAG_EXTRACTIONS, cost);

        return cost < 0 ? getMedia() : cost;
    }

    @Override
    public long insertMedia(long amount, boolean simulate) {
        ItemCreativeUnlocker.addToLongArray(creativeUnlocker, ItemCreativeUnlocker.TAG_INSERTIONS, amount);

        return amount;
    }
}
