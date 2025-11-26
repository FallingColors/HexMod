package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An iota storing a continuation (in essence an execution state).
 */
public class ContinuationIota extends Iota {
    public static final Component DISPLAY = Component.translatable("hexcasting.tooltip.jump_iota").withStyle(ChatFormatting.RED);
    private SpellContinuation value;

    public ContinuationIota(SpellContinuation cont) {
        super(() -> HexIotaTypes.CONTINUATION);
        this.value = cont;
    }

    public SpellContinuation getContinuation() {
        return value;
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

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public Component display() {
        return DISPLAY;
    }

    public static IotaType<ContinuationIota> TYPE = new IotaType<>() {
        public static final MapCodec<ContinuationIota> CODEC = SpellContinuation.getCODEC()
                .xmap(ContinuationIota::new, ContinuationIota::getContinuation)
                .fieldOf("value");
        public static final StreamCodec<RegistryFriendlyByteBuf, ContinuationIota> STREAM_CODEC =
                SpellContinuation.getSTREAM_CODEC().map(ContinuationIota::new, ContinuationIota::getContinuation);

        @Override
        public MapCodec<ContinuationIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ContinuationIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_cc0000;
        }
    };
}
