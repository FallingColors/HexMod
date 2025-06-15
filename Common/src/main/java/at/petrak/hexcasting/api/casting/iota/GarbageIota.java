package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

/**
 * this is LITERALLY a copy of NullIota but I can't see how to do it any better, i hate java generics
 */
public class GarbageIota extends Iota {
    public static final GarbageIota INSTANCE = new GarbageIota();

    private static final Object NULL_SUBSTITUTE = new Object();

    public static final Component DISPLAY = Component.literal("arimfexendrapuse")
        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.OBFUSCATED);

    private GarbageIota() {
        // We have to pass *something* here, but there's nothing that actually needs to go there,
        // so we just do this i guess
        super(() -> HexIotaTypes.GARBAGE);
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that);
    }

    @Override
    public int hashCode() {
        return NULL_SUBSTITUTE.hashCode();
    }

    @Override
    public Component display() {
        return DISPLAY;
    }

    public static IotaType<GarbageIota> TYPE = new IotaType<>() {
        public static final MapCodec<GarbageIota> CODEC = MapCodec.unit(GarbageIota.INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, GarbageIota> STREAM_CODEC =
                StreamCodec.unit(GarbageIota.INSTANCE);

        @Override
        public MapCodec<GarbageIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GarbageIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_505050;
        }
    };
}
