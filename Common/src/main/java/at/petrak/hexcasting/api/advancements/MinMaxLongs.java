package at.petrak.hexcasting.api.advancements;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public record MinMaxLongs(Optional<Long> min, Optional<Long> max, Optional<Long> minSq, Optional<Long> maxSq) implements MinMaxBounds<Long> {
    public static final MinMaxLongs ANY = new MinMaxLongs(null, null);

    public static final Codec<MinMaxLongs> CODEC = MinMaxBounds.createCodec(Codec.LONG, MinMaxLongs::new);

    private MinMaxLongs(Optional<Long> longA, Optional<Long> longB) {
        this(longA, longB, squareOpt(longA), squareOpt(longB));
    }
    private static MinMaxLongs create(StringReader reader, Optional<Long> min, Optional<Long> max) throws CommandSyntaxException {
        if (min.isPresent() && max.isPresent() && min.get() > max.get()) {
            throw ERROR_SWAPPED.createWithContext(reader);
        } else {
            return new MinMaxLongs(min, max);
        }
    }




    private static Optional<Long> squareOpt(Optional<Long> l) {
        return l.map((double_) -> double_ * double_);
    }

    public static MinMaxLongs exactly(long l) {
        return new MinMaxLongs(Optional.of(l), Optional.of(l));
    }

    public static MinMaxLongs between(long min, long max) {
        return new MinMaxLongs(Optional.of(min), Optional.of(max));
    }

    public static MinMaxLongs atLeast(long min) {
        return new MinMaxLongs(Optional.of(min), null);
    }

    public static MinMaxLongs atMost(long max) {
        return new MinMaxLongs(null, Optional.of(max));
    }

    public boolean matches(long l) {
        if (this.min.isPresent() && this.min.get() > l) {
            return false;
        } else {
            return this.max.isEmpty() || this.max.get() >= l;
        }
    }

    public boolean matchesSqr(long l) {
        if (this.minSq.isPresent() && this.minSq.get() > l) {
            return false;
        } else {
            return this.maxSq.isEmpty() || this.maxSq.get() >= l;
        }
    }

    public static MinMaxLongs fromReader(StringReader reader) throws CommandSyntaxException {
        return fromReader(reader, (l) -> l);
    }

    public static MinMaxLongs fromReader(StringReader reader, Function<Long, Long> map) throws CommandSyntaxException {
        BuiltInExceptionProvider builtInExceptions = CommandSyntaxException.BUILT_IN_EXCEPTIONS;
        Objects.requireNonNull(builtInExceptions);
        return MinMaxBounds.fromReader(reader, MinMaxLongs::create, Long::parseLong, builtInExceptions::readerInvalidLong, map);
    }

    @Override
    public Optional<Long> min() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> max() {
        return Optional.empty();
    }
}
