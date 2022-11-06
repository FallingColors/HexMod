package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import net.minecraft.world.item.ItemStack;

public record DebugUnlockerHolder(ItemStack creativeUnlocker) implements ADMediaHolder {
    @Override
    public int getMedia() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxMedia() {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public void setMedia(int media) {
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
    public int withdrawMedia(int cost, boolean simulate) {
        ItemCreativeUnlocker.addToIntArray(creativeUnlocker, ItemCreativeUnlocker.TAG_EXTRACTIONS, cost);

        return cost < 0 ? getMedia() : cost;
    }

    @Override
    public int insertMedia(int amount, boolean simulate) {
        ItemCreativeUnlocker.addToIntArray(creativeUnlocker, ItemCreativeUnlocker.TAG_INSERTIONS, amount);

        return amount;
    }
}
