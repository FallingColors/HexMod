package at.petrak.hexcasting.api.advancements;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.MinMaxBounds;

<<<<<<< HEAD
import javax.annotation.Nullable;
import java.util.Objects;
=======
>>>>>>> refs/remotes/slava/devel/port-1.21
import java.util.Optional;
import java.util.function.Function;

public record MinMaxLongs(Optional<Long> min, Optional<Long> max, Optional<Long> minSq, Optional<Long> maxSq) implements MinMaxBounds<Long> {
<<<<<<< HEAD
    public static final MinMaxLongs ANY = new MinMaxLongs(null, null);

    public static final Codec<MinMaxLongs> CODEC = MinMaxBounds.createCodec(Codec.LONG, MinMaxLongs::new);

    private MinMaxLongs(Optional<Long> longA, Optional<Long> longB) {
        this(longA, longB, squareOpt(longA), squareOpt(longB));
    }
=======
    public static final Codec<MinMaxLongs> CODEC = MinMaxBounds.<Long, MinMaxLongs>createCodec(Codec.LONG, MinMaxLongs::new);

    public static final MinMaxLongs ANY = new MinMaxLongs(Optional.empty(), Optional.empty());

    private MinMaxLongs(Optional<Long> min, Optional<Long> max) {
        this(min, max, squareOpt(min), squareOpt(max));
    }

>>>>>>> refs/remotes/slava/devel/port-1.21
    private static MinMaxLongs create(StringReader reader, Optional<Long> min, Optional<Long> max) throws CommandSyntaxException {
        if (min.isPresent() && max.isPresent() && min.get() > max.get()) {
            throw ERROR_SWAPPED.createWithContext(reader);
        } else {
            return new MinMaxLongs(min, max);
        }
    }

<<<<<<< HEAD



    private static Optional<Long> squareOpt(Optional<Long> l) {
        return l.map((double_) -> double_ * double_);
=======
    private static Optional<Long> squareOpt(Optional<Long> value) {
        return value.map(p_297909_ -> p_297909_ * p_297909_);
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    public static MinMaxLongs exactly(long l) {
        return new MinMaxLongs(Optional.of(l), Optional.of(l));
    }

    public static MinMaxLongs between(long min, long max) {
        return new MinMaxLongs(Optional.of(min), Optional.of(max));
    }

    public static MinMaxLongs atLeast(long min) {
<<<<<<< HEAD
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
=======
        return new MinMaxLongs(Optional.of(min), Optional.empty());
    }

    public static MinMaxLongs atMost(long max) {
        return new MinMaxLongs(Optional.empty(), Optional.of(max));
    }

    public boolean matches(long value) {
        return (this.min.isEmpty() || this.min.get() <= value) && (this.max.isEmpty() || this.max.get() >= value);
    }

    public boolean matchesSqr(long value) {
        return (this.minSq.isEmpty() || this.minSq.get() <= value) && (this.maxSq.isEmpty() || this.maxSq.get() >= value);
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    public static MinMaxLongs fromReader(StringReader reader) throws CommandSyntaxException {
        return fromReader(reader, (l) -> l);
    }

<<<<<<< HEAD
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
=======
    public static MinMaxLongs fromReader(StringReader reader, Function<Long, Long> formatter) throws CommandSyntaxException {
        return MinMaxBounds.fromReader(
                reader, MinMaxLongs::create, Long::parseLong, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidLong, formatter
        );
>>>>>>> refs/remotes/slava/devel/port-1.21
    }
}
