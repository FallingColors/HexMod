package at.petrak.hexcasting.api.spell.datum;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public abstract class SpellDatum {
    @NotNull
    protected final Object datum;

    protected SpellDatum(@NotNull Object datum) {
        this.datum = datum;
    }

    public @NotNull Object getDatum() {
        return datum;
    }

    /**
     * Compare this to another object, within a tolerance.
     * <p>
     * Don't call this directly; use {@link SpellDatums#equalsWithTolerance}.
     */
    abstract public boolean equalsOther(SpellDatum that);


    abstract public @NotNull Tag serialize();

    public interface Type<T extends SpellDatum> {
        /**
         * Spell datums are stored as such: {@code { "type": "modid:type", "datum": a_tag }}.
         * <p>
         * The {@code type} key is given when registering the spell datum type; this method
         * deserializes the tag associated with {@code "datum"}.
         * <p>
         * Returning {@code null} makes the resulting datum be {@link at.petrak.hexcasting.api.spell.Widget Widget.NULL}.
         * Throwing an exception raises a mishap.
         */
        @Nullable
        T deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException;

        /**
         * Get a display of this datum from the tag, <i>without</i> the world.
         * This is for use on the client.
         */
        Component display(Tag tag);

        /**
         * Get the color associated with this datum type.
         */
        int color();
    }
}
