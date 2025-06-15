package at.petrak.hexcasting.api.advancements;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.MinMaxBounds;

import java.util.Optional;
import java.util.function.Function;

public record MinMaxLongs(Optional<Long> min, Optional<Long> max, Optional<Long> minSq, Optional<Long> maxSq) implements MinMaxBounds<Long> {
    public static final Codec<MinMaxLongs> CODEC = MinMaxBounds.<Long, MinMaxLongs>createCodec(Codec.LONG, MinMaxLongs::new);

    public static final MinMaxLongs ANY = new MinMaxLongs(Optional.empty(), Optional.empty());

    private MinMaxLongs(Optional<Long> min, Optional<Long> max) {
        this(min, max, squareOpt(min), squareOpt(max));
    }

    private static MinMaxLongs create(StringReader reader, Optional<Long> min, Optional<Long> max) throws CommandSyntaxException {
        if (min.isPresent() && max.isPresent() && min.get() > max.get()) {
            throw ERROR_SWAPPED.createWithContext(reader);
        } else {
            return new MinMaxLongs(min, max);
        }
    }

    private static Optional<Long> squareOpt(Optional<Long> value) {
        return value.map(p_297909_ -> p_297909_ * p_297909_);
    }

    public static MinMaxLongs exactly(long l) {
        return new MinMaxLongs(Optional.of(l), Optional.of(l));
    }

    public static MinMaxLongs between(long min, long max) {
        return new MinMaxLongs(Optional.of(min), Optional.of(max));
    }

    public static MinMaxLongs atLeast(long min) {
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
    }

    public static MinMaxLongs fromReader(StringReader reader) throws CommandSyntaxException {
        return fromReader(reader, (l) -> l);
    }

    public static MinMaxLongs fromReader(StringReader reader, Function<Long, Long> formatter) throws CommandSyntaxException {
        return MinMaxBounds.fromReader(
                reader, MinMaxLongs::create, Long::parseLong, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidLong, formatter
        );
    }
}
