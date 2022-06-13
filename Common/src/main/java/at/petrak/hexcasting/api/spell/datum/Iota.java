package at.petrak.hexcasting.api.spell.datum;

import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

public abstract class Iota {
    @NotNull
    protected final Object payload;

    protected Iota(@NotNull Object payload) {
        this.payload = payload;
    }

    public @NotNull Object getPayload() {
        return payload;
    }

    /**
     * Compare this to another object, within a tolerance.
     * <p>
     * Don't call this directly; use {@link SpellDatums#equalsWithTolerance}.
     */
    abstract public boolean toleratesOther(Iota that);


    abstract public @NotNull Tag serialize();

}
