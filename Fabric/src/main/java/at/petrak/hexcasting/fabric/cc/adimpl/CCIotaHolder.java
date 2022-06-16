package at.petrak.hexcasting.fabric.cc.adimpl;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.spell.iota.Iota;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class CCIotaHolder extends ItemComponent implements ADIotaHolder {
    public CCIotaHolder(ItemStack stack) {
        super(stack, HexCardinalComponents.IOTA_HOLDER);
    }

    public static class ItemBased extends CCIotaHolder {
        private final IotaHolderItem iotaHolder;

        public ItemBased(ItemStack stack) {
            super(stack);
            if (!(stack.getItem() instanceof IotaHolderItem data)) {
                throw new IllegalStateException("item is not a data holder: " + stack);
            }
            this.iotaHolder = data;
        }

        @Override
        public @Nullable CompoundTag readIotaTag() {
            return this.iotaHolder.readIotaTag(this.stack);
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
    }

    public static class Static extends CCIotaHolder {
        private final Function<ItemStack, Iota> provider;

        public Static(ItemStack stack, Function<ItemStack, Iota> provider) {
            super(stack);
            this.provider = provider;
        }

        @Override
        public @Nullable CompoundTag readIotaTag() {
            var iota = this.provider.apply(this.stack);
            return iota == null ? null : HexIotaTypes.serialize(iota);
        }

        @Override
        public boolean writeIota(@Nullable Iota datum, boolean simulate) {
            return false;
        }
    }
}
