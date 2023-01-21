package at.petrak.hexcasting.api.casting.castables;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Special handling of a pattern. Before checking any of the normal angle-signature based patterns,
 * a given pattern is run by all of these special handlers patterns. If none of them return non-null,
 * then its signature is checked.
 * <p>
 * In the base mod, this is used for number patterns and Bookkeeper's Gambit.
 * <p>
 * There's a separation between the special handlers and their factories so we never have to use
 * {@link Action} instances on the client. We can have SpecialHandlers on the client though because they're just
 * wrappers.
 */
public interface SpecialHandler {
    /**
     * Convert this to an action, for modification of the stack and state.
     * <p>
     * This is called on the SERVER-SIDE ONLY.
     */
    Action act();

    /**
     * Get the name of this handler.
     */
    Component getName();

    /**
     * Given a pattern, possibly make up the special handler from it.
     * <p>
     * This is what goes in the registry! Think of it like BlockEntityType vs BlockEntity.
     */
    @FunctionalInterface
    public interface Factory<T extends SpecialHandler> {
        @Nullable T tryMatch(HexPattern pattern);
    }
}
