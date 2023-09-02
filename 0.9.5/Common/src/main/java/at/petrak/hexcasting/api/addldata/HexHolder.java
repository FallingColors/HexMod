package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.spell.SpellDatum;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HexHolder {

    boolean canDrawManaFromInventory();

    boolean hasHex();

    @Nullable
    List<SpellDatum<?>> getHex(ServerLevel level);

    void writeHex(List<SpellDatum<?>> patterns, int mana);

    void clearHex();
}
