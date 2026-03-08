package at.petrak.hexcasting.fabric.cc.adimpl;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;

import java.util.function.Function;

public abstract class CCItemIotaHolder implements CCIotaHolder, Component {
    final ItemStack stack;
    public CCItemIotaHolder(ItemStack stack) {
        this.stack = stack;
    }

    public static class ItemBased extends CCItemIotaHolder {
        private final IotaHolderItem iotaHolder;

        public ItemBased(ItemStack stack) {
            super(stack);
            if (!(stack.getItem() instanceof IotaHolderItem data)) {
                throw new IllegalStateException("item is not a data holder: " + stack);
            }
            this.iotaHolder = data;
        }

        @Override
        public @Nullable Iota readIota() {
            return this.iotaHolder.readIota(this.stack);
        }

        @Override
        public boolean writeable() {
            return this.iotaHolder.writeable(this.stack);
        }

        @Override
        public boolean writeIota(@Nullable Iota iota, boolean simulate) {
            var canWrite = this.iotaHolder.canWrite(this.stack, iota);
            if (!canWrite) {
                return false;
            }
            if (!simulate) {
                this.iotaHolder.writeDatum(this.stack, iota);
            }
            return true;
        }

        @Override
        public void readFromNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {

        }

        @Override
        public void writeToNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {

        }
    }

    public static class Static extends CCItemIotaHolder {
        private final Function<ItemStack, Iota> provider;

        public Static(ItemStack stack, Function<ItemStack, Iota> provider) {
            super(stack);
            this.provider = provider;
        }

        @Override
        public @Nullable Iota readIota() {
            return this.provider.apply(this.stack);
        }

        @Override
        public boolean writeable() {
            return false;
        }

        @Override
        public boolean writeIota(@Nullable Iota datum, boolean simulate) {
            return false;
        }

        @Override
        public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {

        }

        @Override
        public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {

        }
    }
}
