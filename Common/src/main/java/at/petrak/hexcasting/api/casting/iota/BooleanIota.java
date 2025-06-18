package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class BooleanIota extends Iota {
    private boolean value;
    public BooleanIota(boolean d) {
        super(() -> HexIotaTypes.BOOLEAN);
        this.value = d;
    }

    public boolean getBool() {
        return value;
    }

    @Override
    public boolean isTruthy() {
        return this.getBool();
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
            && that instanceof BooleanIota b
            && this.getBool() == b.getBool();
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public Component display() {
        return BooleanIota.display(getBool());
    }

    public static IotaType<BooleanIota> TYPE = new IotaType<>() {
        public static final MapCodec<BooleanIota> CODEC = Codec.BOOL
                .xmap(BooleanIota::new, BooleanIota::getBool)
                .fieldOf("value");
        public static final StreamCodec<RegistryFriendlyByteBuf, BooleanIota> STREAM_CODEC =
                ByteBufCodecs.BOOL.map(BooleanIota::new, BooleanIota::getBool).mapStream(buffer -> buffer);

        @Override
        public MapCodec<BooleanIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BooleanIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            // We can't set red or green ... so do yellow, I guess
            return 0xff_ffff55;
        }
    };

    public static BooleanIota deserialize(Tag tag) throws IllegalArgumentException {
        var dtag = HexUtils.downcast(tag, ByteTag.TYPE);
        return new BooleanIota(dtag.getAsByte() != 0);
    }

    public static Component display(boolean b) {
        return Component.translatable(b ? "hexcasting.tooltip.boolean_true" : "hexcasting.tooltip.boolean_false")
            .withStyle(b ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED);
    }
}
