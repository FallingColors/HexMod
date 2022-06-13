package at.petrak.hexcasting.api.spell.datum;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatumNull extends Iota {
    private static final Object NULL_SUBSTITUTE = new Object();

    public DatumNull() {
        super(NULL_SUBSTITUTE);
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return that instanceof DatumNull;
    }

    @Override
    public @NotNull Tag serialize() {
        return new CompoundTag();
    }

    public static IotaType<DatumNull> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public DatumNull deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return new DatumNull();
        }

        @Override
        public Component display(Tag tag) {
            return null;
        }

        @Override
        public int color() {
            return 0;
        }
    };
}
