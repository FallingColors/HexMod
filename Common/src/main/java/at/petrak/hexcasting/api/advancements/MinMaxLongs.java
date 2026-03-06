package at.petrak.hexcasting.api.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Replacement for the old MinMaxBounds<Long> now that MinMaxBounds is an interface in 1.21.
 *
 * JSON form matches vanilla MinMaxBounds: either a number, or an object with "min"/"max".
 */
public record MinMaxLongs(Optional<Long> min, Optional<Long> max) implements MinMaxBounds<Long> {
    public static final MinMaxLongs ANY = new MinMaxLongs(Optional.empty(), Optional.empty());

    public static final Codec<MinMaxLongs> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.LONG.optionalFieldOf("min").forGetter(MinMaxLongs::min),
        Codec.LONG.optionalFieldOf("max").forGetter(MinMaxLongs::max)
    ).apply(inst, MinMaxLongs::new));

    public MinMaxLongs(@Nullable Long min, @Nullable Long max) {
        this(Optional.ofNullable(min), Optional.ofNullable(max));
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

    public boolean isAny() {
        return min.isEmpty() && max.isEmpty();
    }

    public boolean matches(long l) {
        if (min.isPresent() && min.get() > l) return false;
        return max.isEmpty() || max.get() >= l;
    }

    public boolean matchesSqr(long l) {
        Long minSq = min.map(v -> v * v).orElse(null);
        Long maxSq = max.map(v -> v * v).orElse(null);
        if (minSq != null && minSq > l) return false;
        return maxSq == null || maxSq >= l;
    }

    public static MinMaxLongs fromJson(@Nullable JsonElement json) {
        if (json == null || json.isJsonNull()) return ANY;
        if (json.isJsonPrimitive()) {
            long v = GsonHelper.convertToLong(json, "value");
            return exactly(v);
        }
        JsonObject obj = GsonHelper.convertToJsonObject(json, "range");
        Long min = obj.has("min") ? GsonHelper.getAsLong(obj, "min") : null;
        Long max = obj.has("max") ? GsonHelper.getAsLong(obj, "max") : null;
        return new MinMaxLongs(min, max);
    }

    public static MinMaxLongs fromReader(StringReader reader) throws CommandSyntaxException {
        // Minimal parser: supports "5", "5..", "..5", "5..10"
        int start = reader.getCursor();
        String remaining = reader.getRemaining();
        int space = remaining.indexOf(' ');
        String token = space >= 0 ? remaining.substring(0, space) : remaining;
        reader.setCursor(start + token.length());

        int dots = token.indexOf("..");
        if (dots < 0) {
            long v = Long.parseLong(token);
            return exactly(v);
        }
        String left = token.substring(0, dots);
        String right = token.substring(dots + 2);
        Long min = left.isEmpty() ? null : Long.parseLong(left);
        Long max = right.isEmpty() ? null : Long.parseLong(right);
        if (min != null && max != null && min > max) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(reader, token);
        }
        return new MinMaxLongs(min, max);
    }
}
