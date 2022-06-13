package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoubleIota extends Iota {
    public static final double TOLERANCE = 0.0001;

    public DoubleIota(double d) {
        super(HexIotaTypes.DOUBLE, d);
    }

    public double getDouble() {
        return HexUtils.fixNAN((Double) this.payload);
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
            && that instanceof DoubleIota dd
            && Math.abs(this.getDouble() - dd.getDouble()) < TOLERANCE;
    }

    @Override
    public @NotNull Tag serialize() {
        return DoubleTag.valueOf(this.getDouble());
    }

    public static IotaType<DoubleIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public DoubleIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var dtag = HexUtils.downcast(tag, DoubleTag.TYPE);
            return new DoubleIota(dtag.getAsDouble());
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
