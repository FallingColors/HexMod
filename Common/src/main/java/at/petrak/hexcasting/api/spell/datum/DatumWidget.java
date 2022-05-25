package at.petrak.hexcasting.api.spell.datum;

import at.petrak.hexcasting.api.spell.Widget;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * Note this is different than the widget itself; this is a widget wrapper.
 */
public class DatumWidget extends SpellDatum {
    public DatumWidget(@NotNull Widget widget) {
        super(widget);
    }

    public Widget getWidget() {
        return (Widget) this.datum;
    }

    @Override
    public Tag serialize() {
        return null;
    }
}
