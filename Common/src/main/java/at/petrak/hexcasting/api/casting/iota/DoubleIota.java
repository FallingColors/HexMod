package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoubleIota extends Iota {
    public static final double TOLERANCE = 0.0001;
    private double value;

    public DoubleIota(double d) {
        super(() -> HexIotaTypes.DOUBLE);
        this.value = d;
    }

    public double getDouble() {
        return HexUtils.fixNAN((Double) value);
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
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public Component display() {
        return DoubleIota.display(value);
    }

    public static IotaType<DoubleIota> TYPE = new IotaType<>() {
        public static final MapCodec<DoubleIota> CODEC = Codec.DOUBLE
                .xmap(DoubleIota::new, DoubleIota::getDouble)
                .fieldOf("value");
        public static final StreamCodec<RegistryFriendlyByteBuf, DoubleIota> STREAM_CODEC =
                ByteBufCodecs.DOUBLE.map(DoubleIota::new, DoubleIota::getDouble).mapStream(buffer -> buffer);

        @Override
        public MapCodec<DoubleIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DoubleIota> streamCodec() {
            return STREAM_CODEC;
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
        return Component.literal(String.format("%.2f", d)).withStyle(ChatFormatting.GREEN);
    }
}
