package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatternIota extends Iota {
    public PatternIota(@NotNull HexPattern pattern) {
        super(HexIotaTypes.PATTERN, pattern);
    }

    public HexPattern getPattern() {
        return (HexPattern) this.payload;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that)
            && that instanceof PatternIota piota
            && this.getPattern().anglesSignature().equals(piota.getPattern().anglesSignature());
    }

    @Override
    public @NotNull Tag serialize() {
        return this.getPattern().serializeToNBT();
    }

    public static IotaType<PatternIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public PatternIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var patTag = HexUtils.downcast(tag, CompoundTag.TYPE);
            HexPattern pat = HexPattern.fromNBT(patTag);
            return new PatternIota(pat);
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
