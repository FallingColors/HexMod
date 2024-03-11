package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface ADIotaHolder {
    @Nullable
    CompoundTag readIotaTag();

    @Nullable
    default Iota readIota(ServerLevel world) {
        var tag = readIotaTag();
        if (tag != null) {
            return IotaType.deserialize(tag, world);
        } else {
            return null;
        }
    }

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
}
