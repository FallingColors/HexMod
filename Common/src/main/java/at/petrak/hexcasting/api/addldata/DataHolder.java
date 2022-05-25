package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.spell.SpellDatum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface DataHolder {
    @Nullable
    CompoundTag readRawDatum();

    @Nullable
    default SpellDatum<?> readDatum(ServerLevel world) {
        var tag = readRawDatum();
        if (tag != null) {
            return SpellDatum.fromNBT(tag, world);
        } else {
            return null;
        }
    }

    @Nullable
    default SpellDatum<?> emptyDatum() {
        return null;
    }

    boolean writeDatum(@Nullable SpellDatum<?> datum, boolean simulate);
}
