package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexContinuationTypes;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

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

    /**
     * @deprecated
     * Use {@link ContinuationIota#TYPE#getCodec} instead.
     */
    @Deprecated
    @Override
    public @NotNull Tag serialize() {
        return HexUtils.serializeWithCodec(this, TYPE.getCodec());
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
        public Codec<ContinuationIota> getCodec() {
            return SpellContinuation.getCodec().xmap(ContinuationIota::new, ContinuationIota::getContinuation);
        }

        @Override
        public Codec<ContinuationIota> getCodec(ServerLevel world) {
            return SpellContinuation.getCodec(world).xmap(ContinuationIota::new, ContinuationIota::getContinuation);
        }

        /**
         * @deprecated
         * Use {@link DoubleIota#TYPE#getCodec} instead.
         */
        @Deprecated
        @Nullable
        @Override
        public ContinuationIota deserialize(Tag tag, ServerLevel world) throws IllegalArgumentException {
            return HexUtils.deserializeWithCodec(tag, getCodec());
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
