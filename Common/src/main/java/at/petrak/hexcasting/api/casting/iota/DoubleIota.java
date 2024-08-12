package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NbtOps;
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

    /**
     * @deprecated
     * Use {@link DoubleIota#TYPE#getCodec()} instead.
     */
    @Deprecated
    @Override
    public @NotNull Tag serialize() {
        return HexUtils.serializeWithCodec(this, TYPE.getCodec());
    }

    public static IotaType<DoubleIota> TYPE = new IotaType<>() {

        @Override
        public Codec<DoubleIota> getCodec() {
            return Codec.DOUBLE.xmap(DoubleIota::new, DoubleIota::getDouble);
        }

        /**
         * @deprecated
         * Use {@link DoubleIota#TYPE#getCodec} instead.
         */
        @Deprecated
        @Nullable
        @Override
        public DoubleIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return HexUtils.deserializeWithCodec(tag, getCodec(world));
        }

        @Override
        public Component display(Tag tag) {
            return DoubleIota.display(HexUtils.deserializeWithCodec(tag, getCodec()).getDouble());
        }

        @Override
        public int color() {
            return 0xff_55ff55;
        }
    };


    public static Component display(double d) {
        return Component.literal(String.format("%.2f", d)).withStyle(ChatFormatting.GREEN);
    }
}
