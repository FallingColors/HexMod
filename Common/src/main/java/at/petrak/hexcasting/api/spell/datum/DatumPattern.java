package at.petrak.hexcasting.api.spell.datum;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

public class DatumPattern extends SpellDatum {
    public DatumPattern(@NotNull HexPattern pattern) {
        super(pattern);
    }

    public HexPattern getPattern() {
        return (HexPattern) this.datum;
    }

    @Override
    public @NotNull Tag serialize() {
        return this.getPattern().serializeToNBT();
    }
}
