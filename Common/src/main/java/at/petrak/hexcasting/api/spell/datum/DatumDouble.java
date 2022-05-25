package at.petrak.hexcasting.api.spell.datum;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatumDouble extends SpellDatum {
    public static final double TOLERANCE = 0.0001;

    public DatumDouble(double d) {
        super(d);
    }

    public double getDouble() {
        return (Double) this.datum;
    }

    @Override
    public boolean equalsOther(SpellDatum that) {
        return that instanceof DatumDouble dd && Math.abs(this.getDouble() - dd.getDouble()) < TOLERANCE;
    }

    @Override
    public @NotNull Tag serialize() {
        return DoubleTag.valueOf(this.getDouble());
    }

    public static SpellDatum.Type<DatumDouble> TYPE = new Type<>() {
        @Nullable
        @Override
        public DatumDouble deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var dtag = (DoubleTag) tag;
            return new DatumDouble(dtag.getAsDouble());
        }

        @Override
        public Component display(Tag tag) {
            return null;
        }

        @Override
        public int color() {
            return 0;
        }
    };
}
