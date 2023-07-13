package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    @Override
    public @NotNull CastResult execute(CastingVM vm, ServerLevel world, SpellContinuation continuation) {
        return new CastResult(this, this.getContinuation(), vm.getImage(), List.of(), ResolvedPatternType.EVALUATED, HexEvalSounds.HERMES);
    }

    @Override
    public boolean executable() {
        return true;
    }

    @Override
    public int size() {
        var continuation = this.getContinuation();
        var size = 0;
        while (continuation instanceof SpellContinuation.NotDone notDone) {
            size += 1;
            size += notDone.component1().size();
            continuation = notDone.component2();
        }

        return Math.min(size, 1);
    }

    public static IotaType<ContinuationIota> TYPE = new IotaType<>() {
        @Override
        public @NotNull ContinuationIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
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
