package at.petrak.hexcasting.api;

import at.petrak.hexcasting.api.spell.Action;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import org.jetbrains.annotations.Nullable;

/**
 * Special handling of a pattern. Before checking any of the normal angle-signature based patterns,
 * a given pattern is run by all of these special handlers patterns. If none of them return non-null,
 * then its signature is checked.
 * <p>
 * In the base mod, this is used for number patterns and Bookkeeper's Gambit.
 */
@FunctionalInterface
public interface SpecialHandler {
    @Nullable Action handlePattern(HexPattern pattern);
}
