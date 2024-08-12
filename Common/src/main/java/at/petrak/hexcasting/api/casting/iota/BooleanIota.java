package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BooleanIota extends Iota {
    public BooleanIota(boolean d) {
        super(HexIotaTypes.BOOLEAN, d);
    }

    public boolean getBool() {
        return (boolean) this.payload;
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

    /**
     * @deprecated
     * Use {@link BooleanIota#TYPE#getCodec} instead.
     */
    @Deprecated
    @Override
    public @NotNull Tag serialize() {
        return HexUtils.serializeWithCodec(this, TYPE.getCodec());
    }

    public static IotaType<BooleanIota> TYPE = new IotaType<>() {

        @Override
        public Codec<BooleanIota> getCodec() {
            return Codec.BOOL.xmap(BooleanIota::new, BooleanIota::getBool);
        }

        /**
         * @deprecated
         * Use {@link BooleanIota#TYPE#getCodec} instead.
         */
        @Deprecated
        @Nullable
        @Override
        public BooleanIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return HexUtils.deserializeWithCodec(tag, getCodec(world));
        }


        @Override
        public Component display(Tag tag) {
            return BooleanIota.display(HexUtils.deserializeWithCodec(tag, getCodec()).getBool());
        }

        @Override
        public int color() {
            // We can't set red or green ... so do yellow, I guess
            return 0xff_ffff55;
        }
    };

    public static Component display(boolean b) {
        return Component.translatable(b ? "hexcasting.tooltip.boolean_true" : "hexcasting.tooltip.boolean_false")
            .withStyle(b ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED);
    }
}
