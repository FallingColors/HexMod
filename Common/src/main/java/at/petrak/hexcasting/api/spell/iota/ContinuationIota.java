package at.petrak.hexcasting.api.spell.iota;

import at.petrak.hexcasting.api.spell.casting.SpellContinuation;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An iota storing a continuation (in essence an execution state).
 */
public class ContinuationIota extends Iota {
    public static final Component DISPLAY = Component.translatable("hexcasting.tooltip.jump_iota").withStyle(ChatFormatting.RED);

    public ContinuationIota(SpellContinuation cont) {
        super(HexIotaTypes.CONTINUATION, cont);
    }

    public SpellContinuation getContinuation() {
        return (SpellContinuation) this.payload;
    }

    @Override
    public boolean isTruthy() {
        return true;
    }

    @Override
    public boolean toleratesOther(Iota that) {
        return typesMatch(this, that) && that instanceof ContinuationIota cont && cont.getContinuation().equals(getContinuation());
    }

    @Override
    public @NotNull
    Tag serialize() {
        return getContinuation().serializeToNBT();
    }

    public static IotaType<ContinuationIota> TYPE = new IotaType<>() {
        @Nullable
        @Override
        public ContinuationIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            var compoundTag = HexUtils.downcast(tag, CompoundTag.TYPE);
            return new ContinuationIota(SpellContinuation.fromNBT(compoundTag, world));
        }

        @Override
        public Component display(Tag tag) {
            return DISPLAY;
        }

        @Override
        public int color() {
            return 0xff_cc0000;
        }
    };
}
