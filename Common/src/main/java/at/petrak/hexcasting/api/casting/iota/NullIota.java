package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An iota with no data associated with it.
 */
public class NullIota extends Iota {
    public static final NullIota INSTANCE = new NullIota();
    private static final Object NULL_SUBSTITUTE = new Object();

    public static final Component DISPLAY =
        Component.translatable("hexcasting.tooltip.null_iota").withStyle(ChatFormatting.GRAY);

    private NullIota() {
        super(() -> HexIotaTypes.NULL);
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

    public static IotaType<NullIota> TYPE = new IotaType<>() {
        public static final MapCodec<NullIota> CODEC = MapCodec.unit(NullIota.INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, NullIota> STREAM_CODEC =
                StreamCodec.unit(NullIota.INSTANCE);

        @Override
        public MapCodec<NullIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, NullIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_aaaaaa;
        }
    };
}
