package at.petrak.hexcasting.api.casting;

import at.petrak.hexcasting.api.casting.castables.SpecialHandler;
import net.minecraft.resources.ResourceKey;

/**
 * Possible things we find when trying to match a pattern's shape.
 */
public abstract sealed class PatternShapeMatch {
    /**
     * I've never met that pattern in my life
     */
    public static final class Nothing extends PatternShapeMatch {
    }

    /**
     * The shape exactly matches a pattern that isn't altered per world
     */
    public static final class Normal extends PatternShapeMatch {
        public final ResourceKey<ActionRegistryEntry> key;

        public Normal(ResourceKey<ActionRegistryEntry> key) {
            this.key = key;
        }
    }

    /**
     * The pattern is the right <em>shape</em> to be one of the per-world patterns.
     * <p>
     * On the server, {@link PerWorld#certain} means whether this is an exact match, or if it's just the
     * right shape. (In other words it should only actually be casted if it is true.)
     * <p>
     * On the client, it is always false.
     */
    public static final class PerWorld extends PatternShapeMatch {
        public final ResourceKey<ActionRegistryEntry> key;
        public final boolean certain;

        public PerWorld(ResourceKey<ActionRegistryEntry> key, boolean certain) {
            this.key = key;
            this.certain = certain;
        }
    }

    /**
     * The shape matches a special handler
     */
    public static final class Special extends PatternShapeMatch {
        public final ResourceKey<SpecialHandler.Factory<?>> key;
        public final SpecialHandler handler;

        public Special(ResourceKey<SpecialHandler.Factory<?>> key, SpecialHandler handler) {
            this.key = key;
            this.handler = handler;
        }
    }
}
