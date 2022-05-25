package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.spell.LegacySpellDatum;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface DataHolder {
    @Nullable
    CompoundTag readRawDatum();

    @Nullable
    default LegacySpellDatum<?> readDatum(ServerLevel world) {
        var tag = readRawDatum();
        if (tag != null) {
            return LegacySpellDatum.fromNBT(tag, world);
        } else {
            return null;
        }
    }

    @Nullable
    default LegacySpellDatum<?> emptyDatum() {
        return null;
    }

    boolean writeDatum(@Nullable LegacySpellDatum<?> datum, boolean simulate);
}
