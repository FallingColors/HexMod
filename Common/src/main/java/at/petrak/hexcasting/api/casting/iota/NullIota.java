package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An iota with no data associated with it.
 */
public class NullIota extends Iota {
    private static final Object NULL_SUBSTITUTE = new Object();

    public static final Component DISPLAY =
        Component.translatable("hexcasting.tooltip.null_iota").withStyle(ChatFormatting.GRAY);

    public NullIota() {
        // We have to pass *something* here, but there's nothing that actually needs to go there,
        // so we just do this i guess
        super(HexIotaTypes.NULL, NULL_SUBSTITUTE);
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that);
    }

    /**
     * @deprecated
     * use {@link NullIota#TYPE#getCodec} instead.
     */
    @Deprecated
    @Override
    public @NotNull Tag serialize() {
        return HexUtils.serializeWithCodec(this, TYPE.getCodec());
    }

    public static IotaType<NullIota> TYPE = new IotaType<>() {

        @Override
        public Codec<NullIota> getCodec() {
            return Codec.unit(NullIota::new);
        }

        /**
         * @deprecated
         * use {@link NullIota#TYPE#getCodec} instead.
         */
        @Deprecated
        @Nullable
        @Override
        public NullIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return HexUtils.deserializeWithCodec(tag, getCodec(world));
        }

        @Override
        public Component display(Tag tag) {
            return DISPLAY;
        }

        @Override
        public int color() {
            return 0xff_aaaaaa;
        }
    };
}
