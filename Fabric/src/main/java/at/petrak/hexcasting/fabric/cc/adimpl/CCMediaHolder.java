package at.petrak.hexcasting.fabric.cc.adimpl;

import at.petrak.hexcasting.api.addldata.ADMediaHolder;
import at.petrak.hexcasting.api.item.MediaHolderItem;
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public abstract class CCMediaHolder extends ItemComponent implements ADMediaHolder {
    public CCMediaHolder(ItemStack stack) {
        super(stack, HexCardinalComponents.MEDIA_HOLDER);
    }

    public static class ItemBased extends CCMediaHolder {
        private final MediaHolderItem mediaHolder;

        public ItemBased(ItemStack stack) {
            super(stack);
            if (!(stack.getItem() instanceof MediaHolderItem media)) {
                throw new IllegalStateException("item is not a media holder: " + stack);
            }
            this.mediaHolder = media;
        }

        @Override
        public long getMedia() {
            return this.mediaHolder.getMedia(this.stack);
        }

        @Override
        public long getMaxMedia() {
            return this.mediaHolder.getMaxMedia(this.stack);
        }

        @Override
        public void setMedia(long media) {
            this.mediaHolder.setMedia(this.stack, media);
        }

        @Override
        public boolean canRecharge() {
            return this.mediaHolder.canRecharge(this.stack);
        }

        @Override
        public boolean canProvide() {
            return this.mediaHolder.canProvideMedia(this.stack);
        }

        @Override
        public int getConsumptionPriority() {
            return this.mediaHolder.getConsumptionPriority(this.stack);
        }

        @Override
        public boolean canConstructBattery() {
            return false;
        }

        @Override
        public long withdrawMedia(long cost, boolean simulate) {
            return this.mediaHolder.withdrawMedia(this.stack, cost, simulate);
        }

        @Override
        public long insertMedia(long amount, boolean simulate) {
            return this.mediaHolder.insertMedia(this.stack, amount, simulate);
        }
    }

    public static class Static extends CCMediaHolder {
        private final Supplier<Long> baseWorth;
        private final int consumptionPriority;

        public Static(Supplier<Long> baseWorth, int consumptionPriority, ItemStack stack) {
            super(stack);
            this.baseWorth = baseWorth;
            this.consumptionPriority = consumptionPriority;
        }

        @Override
        public long getMedia() {
            return baseWorth.get() * stack.getCount();
        }

        @Override
        public long getMaxMedia() {
            return getMedia();
        }

        @Override
        public void setMedia(long media) {
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
        public long withdrawMedia(long cost, boolean simulate) {
            long worth = baseWorth.get();
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
}
