package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.casting.iota.Iota;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ADHexHolder {

    boolean canDrawMediaFromInventory();

    boolean hasHex();

    @Nullable
    List<Iota> getHex(ServerLevel level);

    void writeHex(List<Iota> patterns, long media);

    void clearHex();
}
