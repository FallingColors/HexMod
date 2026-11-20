package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface ADIotaHolder {

    @Nullable
    Iota readIota();

    @Nullable
    default Iota emptyIota() {
        return null;
    }

    /**
     * @return if the writing succeeded/would succeed
     */
    boolean writeIota(@Nullable Iota iota, boolean simulate);

    /**
     * @return whether it is possible to write to this IotaHolder
     */
    boolean writeable();

    /**
     * Things that read/write an arbitrary iota onto an itemstack, like foci
     */
    record Dynamic(IotaHolderItem holder, ItemStack stack) implements ADIotaHolder {
        @Override
        public @Nullable
        Iota readIota() {
            return holder.readIota(stack);
        }

        @Override
        public @Nullable
        Iota emptyIota() {
            return holder.emptyIota(stack);
        }

        @Override
        public boolean writeIota(@Nullable Iota iota, boolean simulate) {
            if (!holder.canWrite(stack, iota)) {
                return false;
            }
            if (!simulate) {
                holder.writeDatum(stack, iota);
            }
            return true;
        }

        @Override
        public boolean writeable() {
            return holder.writeable(stack);
        }
    }

    /**
     * Things that always provide a specific iota when read, like pumpkin pie
     */
    record Static(Function<ItemStack, Iota> provider, ItemStack stack) implements ADIotaHolder {
        @Override
        public @Nullable
        Iota readIota() {
            return provider.apply(stack);
        }

        @Override
        public boolean writeable() {
            return false;
        }

        @Override
        public boolean writeIota(@Nullable Iota iota, boolean simulate) {
            return false;
        }
    }
}
