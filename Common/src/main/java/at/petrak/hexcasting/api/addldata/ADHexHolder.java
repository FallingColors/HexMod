package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.spell.iota.Iota;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ADHexHolder {

    boolean canDrawManaFromInventory();

    boolean hasHex();

    @Nullable
    List<Iota> getHex(ServerLevel level);

    void writeHex(List<Iota> patterns, int mana);

    void clearHex();
}
