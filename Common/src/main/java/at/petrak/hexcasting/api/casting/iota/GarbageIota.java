package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * this is LITERALLY a copy of NullIota but I can't see how to do it any better, i hate java generics
 */
public class GarbageIota extends Iota {
    private static final Object NULL_SUBSTITUTE = new Object();

    public static final Component DISPLAY = Component.literal("arimfexendrapuse")
        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.OBFUSCATED);

    private static final Random RANDOM = new Random();

    public GarbageIota() {
        // We have to pass *something* here, but there's nothing that actually needs to go there,
        // so we just do this i guess
        super(HexIotaTypes.GARBAGE, NULL_SUBSTITUTE);
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
    public @NotNull Tag serialize() {
        return new CompoundTag();
    }

    public static IotaType<GarbageIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public GarbageIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return new GarbageIota();
        }

        @Override
        public Component display(Tag tag) {
            return DISPLAY;
        }

        @Override
        public int color() {
            return 0xff_505050;
        }
    };
}
