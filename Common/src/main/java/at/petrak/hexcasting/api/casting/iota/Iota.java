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
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class Iota {
    @NotNull
    protected final Supplier<IotaType<? extends Iota>> type;

    protected Iota(@NotNull Supplier<IotaType<? extends Iota>> type) {
        this.type = type;
    }

    public @NotNull IotaType<? extends Iota> getType() {
        return this.type.get();
    }

    abstract public boolean isTruthy();

    /**
     * Compare this to another object, within a tolerance.
     */
    abstract protected boolean toleratesOther(Iota that);

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

    public int depth() {
        return 1;
    }

    public abstract Component display();

    public FormattedCharSequence displayWithMaxWidth(int maxWidth, Font font) {
        var splitted = font.split(display(), maxWidth - font.width("..."));
        if (splitted.isEmpty())
            return FormattedCharSequence.EMPTY;
        else if (splitted.size() == 1)
            return splitted.getFirst();
        else {
            var first = splitted.getFirst();
            return FormattedCharSequence.fromPair(first,
                    Component.literal("...").withStyle(ChatFormatting.GRAY).getVisualOrderText());
        }
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
    public abstract int hashCode();

    @Override
    public boolean equals(Object o) {
        if(o instanceof Iota io) {
            return tolerates(this, io);
        }
        return false;
    }
}
