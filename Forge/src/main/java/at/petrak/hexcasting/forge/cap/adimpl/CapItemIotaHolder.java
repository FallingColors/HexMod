package at.petrak.hexcasting.forge.cap.adimpl;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.casting.iota.Iota;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record CapItemIotaHolder(IotaHolderItem holder,
                                ItemStack stack) implements ADIotaHolder {

    @Override
    public @Nullable
    CompoundTag readIotaTag() {
        return holder.readIotaTag(stack);
    }

    @Override
    public @Nullable
    Iota readIota(ServerLevel world) {
        return holder.readIota(stack, world);
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
