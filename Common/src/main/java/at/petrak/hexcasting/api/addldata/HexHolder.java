package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface HexHolder {

    boolean canDrawManaFromInventory();

    @Nullable
    List<HexPattern> getPatterns();

    void writePatterns(List<HexPattern> patterns, int mana);

    void clearPatterns();
}
