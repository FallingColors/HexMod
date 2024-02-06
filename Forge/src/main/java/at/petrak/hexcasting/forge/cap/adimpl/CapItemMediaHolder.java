package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import net.minecraft.world.item.ItemStack;

/**
 * Things that read/write media amounts from an itemstack
 */
public record CapItemMediaHolder(MediaHolderItem holder,
                                 ItemStack stack) implements ADMediaHolder {

    @Override
    public long getMedia() {
        return holder.getMedia(stack);
    }

    @Override
    public long getMaxMedia() {
        return holder.getMaxMedia(stack);
    }

    @Override
    public void setMedia(long media) {
        holder.setMedia(stack, media);
    }

    @Override
    public boolean canRecharge() {
        return holder.canRecharge(stack);
    }

    @Override
    public boolean canProvide() {
        return holder.canProvideMedia(stack);
    }

    @Override
    public int getConsumptionPriority() {
        return holder.getConsumptionPriority(stack);
    }

    @Override
    public boolean canConstructBattery() {
        return false;
    }

    @Override
    public long withdrawMedia(long cost, boolean simulate) {
        return holder.withdrawMedia(stack, cost, simulate);
    }

    @Override
    public long insertMedia(long amount, boolean simulate) {
        return holder.insertMedia(stack, amount, simulate);
    }
}
