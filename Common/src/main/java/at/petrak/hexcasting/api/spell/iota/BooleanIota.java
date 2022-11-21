package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ByteTag;
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

    @Override
    public @NotNull Tag serialize() {
        // there is no boolean tag :(
        return ByteTag.valueOf(this.getBool());
    }

    public static IotaType<BooleanIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public BooleanIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return BooleanIota.deserialize(tag);
        }

        @Override
        public Component display(Tag tag) {
            return BooleanIota.display(BooleanIota.deserialize(tag).getBool());
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
