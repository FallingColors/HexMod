package at.petrak.hexcasting.api.casting;

import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexPattern;

/**
 * A bit of wrapper information around an action to go in the registry.
 *
 * @param prototype The pattern associated with this action. The start dir acts as the "canonical" start direction
 *                  for display in the book. For per-world patterns, the angle signature is the *shape* of the pattern
 *                  but probably not the pattern itself.
 * @param action    The action itself
 */
public record ActionRegistryEntry(HexPattern prototype, Action action) {
}
