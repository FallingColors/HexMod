package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
            return PatternIota.deserialize(tag);
        }

        @Override
        public Component display(Tag tag) {
            return PatternIota.display(PatternIota.deserialize(tag).getPattern());
        }

        @Override
        public int color() {
            return 0xff_ffaa00;
        }
    };

    public static PatternIota deserialize(Tag tag) throws IllegalArgumentException {
        var patTag = HexUtils.downcast(tag, CompoundTag.TYPE);
        HexPattern pat = HexPattern.fromNBT(patTag);
        return new PatternIota(pat);
    }

    public static Component display(HexPattern pat) {
        var bob = new StringBuilder("HexPattern(");
        bob.append(pat.getStartDir());

        var sig = pat.anglesSignature();
        if (!sig.isEmpty()) {
            bob.append(" ");
            bob.append(sig);
        }
        bob.append(")");
        return new TextComponent(bob.toString()).withStyle(ChatFormatting.GOLD);
    }
}
