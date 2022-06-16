package at.petrak.hexcasting.api.spell.iota;

import net.minecraft.client.gui.Font;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;

// Take notes from ForgeRegistryEntry
public abstract class IotaType<T extends Iota> {
    /**
     * Spell datums are stored as such: {@code { "type": "modid:type", "datum": a_tag }}.
     * <p>
     * The {@code type} key is given when registering the spell datum type; this method
     * deserializes the tag associated with {@code "datum"}.
     * <p>
     * Returning {@code null} makes the resulting datum be {@link NullIota}.
     * Throwing an exception raises a mishap.
     */
    @Nullable
    public abstract T deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException;

    /**
     * Get a display of this datum from the {@code data} tag, <i>without</i> the world.
     * This is for use on the client.
     */
    public abstract Component display(Tag tag);

    /**
     * Get a display of this datum from the {@code data} tag, with a maximum width.
     * This is for use on the client.
     */
    public List<FormattedCharSequence> displayWithWidth(Tag tag, int maxWidth, Font font) {
        var display = this.display(tag);
        return font.split(display, maxWidth);
    }

    /**
     * Get the color associated with this datum type.
     */
    public abstract int color();
}
