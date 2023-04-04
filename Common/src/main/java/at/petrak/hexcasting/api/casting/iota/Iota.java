package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapUnescapedValue;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Iota {
    @NotNull
    protected final Object payload;
    @NotNull
    protected final IotaType<?> type;

    protected Iota(@NotNull IotaType<?> type, @NotNull Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public @NotNull IotaType<?> getType() {
        return this.type;
    }

    abstract public boolean isTruthy();

    /**
     * Compare this to another object, within a tolerance.
     */
    abstract protected boolean toleratesOther(Iota that);

    /**
     * Serialize this under the {@code data} tag.
     * <p>
     * You probably don't want to call this directly; use {@link IotaType#serialize}.
     */
    abstract public @NotNull Tag serialize();

    /**
     * This method is called when this iota is executed (i.e. Hermes is run on a list containing it, unescaped).
     * By default it will return a {@link CastResult} indicating an error has occurred.
     */
    public @NotNull CastResult execute(CastingVM vm, ServerLevel world, SpellContinuation continuation) {
        return new CastResult(
            continuation,
            null,
            List.of(
                new OperatorSideEffect.DoMishap(
                    new MishapUnescapedValue(this),
                    new Mishap.Context(new HexPattern(HexDir.WEST, List.of()), null)
                )
            ), // Should never matter
            ResolvedPatternType.INVALID,
            HexEvalSounds.MISHAP
        );
    }

    public Component display() {
        return this.type.display(this.serialize());
    }

    /**
     * Helper method to see if two iotas have the same type.
     */
    public static boolean typesMatch(Iota a, Iota b) {
        var resA = HexIotaTypes.REGISTRY.getKey(a.getType());
        var resB = HexIotaTypes.REGISTRY.getKey(b.getType());
        return resA != null && resA.equals(resB);
    }

    /**
     * Helper method to see if either iota tolerates the other.
     */
    public static boolean tolerates(Iota a, Iota b) {
        return a.toleratesOther(b) || b.toleratesOther(a);
    }
}
