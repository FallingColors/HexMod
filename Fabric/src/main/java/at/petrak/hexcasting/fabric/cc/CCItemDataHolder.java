package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.spell.SpellDatum;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class CCItemDataHolder extends ItemComponent implements CCDataHolder {
    public CCItemDataHolder(ItemStack stack) {
        super(stack, HexCardinalComponents.DATA_HOLDER);
    }

    public static class ItemBased extends CCItemDataHolder {
        private final DataHolderItem dataHolder;

        public ItemBased(ItemStack stack) {
            super(stack);
            if (!(stack.getItem() instanceof DataHolderItem data)) {
                throw new IllegalStateException("item is not a data holder: " + stack);
            }
            this.dataHolder = data;
        }

        @Override
        public @Nullable CompoundTag readRawDatum() {
            return this.dataHolder.readDatumTag(this.stack);
        }

        @Override
        public boolean writeDatum(@Nullable SpellDatum<?> datum, boolean simulate) {
            var canWrite = this.dataHolder.canWrite(this.stack, datum);
            if (!canWrite) {
                return false;
            }
            if (!simulate) {
                this.dataHolder.writeDatum(this.stack, datum);
            }
            return true;
        }
    }

    public static class Static extends CCItemDataHolder {
        private final Function<ItemStack, SpellDatum<?>> provider;

        public Static(ItemStack stack, Function<ItemStack, SpellDatum<?>> provider) {
            super(stack);
            this.provider = provider;
        }

        @Override
        public @Nullable CompoundTag readRawDatum() {
            SpellDatum<?> datum = this.provider.apply(this.stack);
            return datum == null ? null : datum.serializeToNBT();
        }

        @Override
        public boolean writeDatum(@Nullable SpellDatum<?> datum, boolean simulate) {
            return false;
        }
    }
}
