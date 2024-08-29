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
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class Iota {

    /**
     * Get a generic Iota codec for encoding/decoding any Iota, without validating iotas.
     * This codec is useful for serialization, and deserialization on the client side.
     * @return an Iota codec.
     */
    public static Codec<Iota> getCodec() {
        return HexIotaTypes.REGISTRY.byNameCodec().<Iota>dispatchMap(HexIotaTypes.KEY_TYPE, Iota::getType,
                iotaType -> iotaType.getCodec().fieldOf(HexIotaTypes.KEY_DATA).codec()).codec();
    }

    /**
     * Get a generic Iota codec for encoding/decoding any Iota, validating iotas using the given world.
     * This codec is useful for deserialization on the server side.
     * @param world the world to be used for validation
     * @return an Iota codec
     */
    public static Codec<Iota> getCodec(@Nullable ServerLevel world) {
        return HexIotaTypes.REGISTRY.byNameCodec().<Iota>dispatchMap(HexIotaTypes.KEY_TYPE, Iota::getType,
                iotaType -> iotaType.getCodec(world).fieldOf(HexIotaTypes.KEY_DATA).codec()).codec().orElseGet(NullIota::new);
    }

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
     *
     * @deprecated
     * Use the Iota's {@code CODEC} instead.
     */
    @Deprecated
    abstract public @NotNull Tag serialize();

    /**
     * This method is called when this iota is executed (i.e. Hermes is run on a list containing it, unescaped).
     * By default it will return a {@link CastResult} indicating an error has occurred.
     */
    public @NotNull CastResult execute(CastingVM vm, ServerLevel world, SpellContinuation continuation) {
        return new CastResult(
                this,
                continuation,
                null,  // Should never matter
                List.of(
                        new OperatorSideEffect.DoMishap(
                                new MishapUnescapedValue(this),
                                new Mishap.Context(new HexPattern(HexDir.WEST, List.of()), null)
                        )
                ),
                ResolvedPatternType.INVALID,
                HexEvalSounds.MISHAP);
    }

    /**
     * Returns whether this iota is possible to execute (i.e. whether {@link Iota#execute} has been overridden.
     */
    public boolean executable() {
        return false;
    }

    /**
     * This method is called to determine whether the iota is above the max serialisation depth/serialisation count
     * limits. It should return every "iota" that is a subelement of this iota.
     * For example, if you implemented a Map&lt;Iota, Iota&gt;, then it should be an iterable over the keys *and*
     * values of the map. If you implemented a typed List&lt;Double&gt; iota for some reason, you should instead override
     * {@link Iota#size}.
     */
    public @Nullable Iterable<Iota> subIotas() {
        return null;
    }

    /**
     * This method is called to determine whether the iota is above the max serialisation depth/serialisation count limits.
     * This is an alternative to deriving subIotas for if your Iota is a datastructure of variable size over something that
     * doesn't make sense to convert to an Iota iterable, such as {@link ContinuationIota}, or a typed List&lt;Double&gt;.
     * It should return "1" per "iota sized" unit of memory that it would occupy. Easy option, return the element count of
     * your data structure.
     */
    public int size() {
        return 1;
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

    @Override
    public int hashCode() {
        return payload.hashCode();
    }
}
