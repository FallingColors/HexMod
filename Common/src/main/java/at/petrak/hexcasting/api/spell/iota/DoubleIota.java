package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
    public boolean isTruthy() {
        return this.getDouble() != 0.0;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
            && that instanceof DoubleIota dd
            && tolerates(this.getDouble(), dd.getDouble());
    }

    public static boolean tolerates(double a, double b) {
        return Math.abs(a - b) < TOLERANCE;
    }

    @Override
    public @NotNull Tag serialize() {
        return DoubleTag.valueOf(this.getDouble());
    }

    public static IotaType<DoubleIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public DoubleIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return DoubleIota.deserialize(tag);
        }

        @Override
        public Component display(Tag tag) {
            return DoubleIota.display(DoubleIota.deserialize(tag).getDouble());
        }

        @Override
        public int color() {
            return 0xff_55ff55;
        }
    };

    public static DoubleIota deserialize(Tag tag) throws IllegalArgumentException {
        var dtag = HexUtils.downcast(tag, DoubleTag.TYPE);
        return new DoubleIota(dtag.getAsDouble());
    }

    public static Component display(double d) {
        return new TextComponent(String.format("%.2f", d)).withStyle(ChatFormatting.GREEN);
    }
}
