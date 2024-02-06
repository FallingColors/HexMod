package at.petrak.hexcasting.api.advancements;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

public class MinMaxLongs extends MinMaxBounds<Long> {
    public static final MinMaxLongs ANY = new MinMaxLongs(null, null);
    @Nullable
    private final Long minSq;
    @Nullable
    private final Long maxSq;

    private static MinMaxLongs create(StringReader reader, @Nullable Long min, @Nullable Long max) throws CommandSyntaxException {
        if (min != null && max != null && min > max) {
            throw ERROR_SWAPPED.createWithContext(reader);
        } else {
            return new MinMaxLongs(min, max);
        }
    }

    @Nullable
    private static Long squareOpt(@Nullable Long l) {
        return l == null ? null : l * l;
    }

    private MinMaxLongs(@Nullable Long min, @Nullable Long max) {
        super(min, max);
        this.minSq = squareOpt(min);
        this.maxSq = squareOpt(max);
    }

    public static MinMaxLongs exactly(long l) {
        return new MinMaxLongs(l, l);
    }

    public static MinMaxLongs between(long min, long max) {
        return new MinMaxLongs(min, max);
    }

    public static MinMaxLongs atLeast(long min) {
        return new MinMaxLongs(min, null);
    }

    public static MinMaxLongs atMost(long max) {
        return new MinMaxLongs(null, max);
    }

    public boolean matches(long l) {
        if (this.min != null && this.min > l) {
            return false;
        } else {
            return this.max == null || this.max >= l;
        }
    }

    public boolean matchesSqr(long l) {
        if (this.minSq != null && this.minSq > l) {
            return false;
        } else {
            return this.maxSq == null || this.maxSq >= l;
        }
    }

    public static MinMaxLongs fromJson(@Nullable JsonElement json) {
        return fromJson(json, ANY, GsonHelper::convertToLong, MinMaxLongs::new);
    }

    public static MinMaxLongs fromReader(StringReader reader) throws CommandSyntaxException {
        return fromReader(reader, (l) -> l);
    }

    public static MinMaxLongs fromReader(StringReader reader, Function<Long, Long> map) throws CommandSyntaxException {
        BuiltInExceptionProvider builtInExceptions = CommandSyntaxException.BUILT_IN_EXCEPTIONS;
        Objects.requireNonNull(builtInExceptions);
        return fromReader(reader, MinMaxLongs::create, Long::parseLong, builtInExceptions::readerInvalidInt, map);
    }
}
