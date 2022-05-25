package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.spell.LegacySpellDatum;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HexHolder {

    boolean canDrawManaFromInventory();

    boolean hasHex();

    @Nullable
    List<LegacySpellDatum<?>> getHex(ServerLevel level);

    void writeHex(List<LegacySpellDatum<?>> patterns, int mana);

    void clearHex();
}
