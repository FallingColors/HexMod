package at.petrak.hexcasting.common.casting;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.SpecialHandler;
import at.petrak.hexcasting.api.casting.math.EulerPathFinder;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.server.ScrungledPatternsSave;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

// Now an internal-only class used to do final processing on the registered stuff
public class PatternRegistryManifest {
    /* Map actions to their entry except for the per-world ones
     *
     * This can be static because the patterns that are per-world don't change in one server lifecycle.
     */
    private static final ConcurrentMap<String, ResourceKey<ActionRegistryEntry>> NORMAL_ACTION_LOOKUP =
        new ConcurrentHashMap<>();

    /**
     * A set of all the per-world patterns. This doesn't store <em>what</em> they are, just that
     * they exist.
     */
    private static final ConcurrentLinkedDeque<ResourceKey<ActionRegistryEntry>> PER_WORLD_ACTIONS =
        new ConcurrentLinkedDeque<>();

    /**
     * Process the registry!
     * <p>
     * Pass null for the OW to signal we're on the client
     */
    public static void processRegistry(@Nullable ServerLevel overworld) {
        ScrungledPatternsSave perWorldPatterns = null;
        if (overworld != null) {
            perWorldPatterns = ScrungledPatternsSave.open(overworld);
        }

        var postCalculationNeeders = new ArrayList<ResourceKey<ActionRegistryEntry>>();

        var registry = IXplatAbstractions.INSTANCE.getActionRegistry();
        for (var key : registry.registryKeySet()) {
            var entry = registry.get(key);
            if (HexUtils.isOfTag(registry, key, HexTags.Actions.PER_WORLD_PATTERN)) {
                PER_WORLD_ACTIONS.add(key);

                // Then we need to create this only on the server, gulp
                if (perWorldPatterns != null) {
                    var precalced = perWorldPatterns.lookup(entry.prototype().anglesSignature());
                    if (precalced == null) {
                        postCalculationNeeders.add(key);
                    }
                } else {
                    // We're on the client, TODO implement the client guessing code
                }

            } else {
                NORMAL_ACTION_LOOKUP.put(entry.prototype().anglesSignature(), key);
            }
        }

        if (perWorldPatterns != null) {
            for (var postNeederKey : postCalculationNeeders) {
                var regiEntry = registry.get(postNeederKey);
                var scrungledPat = scrunglePattern(regiEntry.prototype(), overworld.getSeed());

                perWorldPatterns.insert(scrungledPat, postNeederKey);
            }
        }

        HexAPI.LOGGER.info(("We're on the %s! " +
            "Loaded %d regular actions, %d per-world actions, and %d special handlers").formatted(
            (overworld == null) ? "client" : "server", NORMAL_ACTION_LOOKUP.size(), PER_WORLD_ACTIONS.size(),
            IXplatAbstractions.INSTANCE.getSpecialHandlerRegistry().size()
        ));
    }

    /**
     * Try to match this pattern to a special handler. If one is found, return both the handler and its key.
     */
    @Nullable
    public static Pair<SpecialHandler, ResourceKey<SpecialHandler.Factory<?>>> matchPatternToSpecialHandler(HexPattern pat) {
        var registry = IXplatAbstractions.INSTANCE.getSpecialHandlerRegistry();
        for (var key : registry.registryKeySet()) {
            var factory = registry.get(key);
            var handler = factory.tryMatch(pat);
            if (handler != null) {
                return Pair.of(handler, key);
            }
        }
        return null;
    }

    /**
     * Try to match this pattern to an action, whether via a normal pattern, a per-world pattern, or the machinations
     * of a special handler.
     *
     * @param checkForAlternateStrokeOrders if this is true, will check if the pattern given is an erroneous stroke
     *                                      order
     *                                      for a per-world pattern.
     */
    public static PatternShapeMatch matchPattern(HexPattern pat, ServerLevel overworld,
        boolean checkForAlternateStrokeOrders) {
        // I am PURPOSELY checking normal actions before special handlers
        // This way we don't get a repeat of the phial number literal incident
        var sig = pat.anglesSignature();
        if (NORMAL_ACTION_LOOKUP.containsKey(sig)) {
            var key = NORMAL_ACTION_LOOKUP.get(sig);
            return new PatternShapeMatch.Normal(key);
        }

        // Look it up in the world?
        var perWorldPatterns = ScrungledPatternsSave.open(overworld);
        var entry = perWorldPatterns.lookup(sig);
        if (entry != null) {
            return new PatternShapeMatch.PerWorld(entry.key(), true);
        }

        if (checkForAlternateStrokeOrders) {
            throw new NotImplementedException("checking for alternate stroke orders is NYI sorry");
        }

        var shMatch = matchPatternToSpecialHandler(pat);
        if (shMatch != null) {
            return new PatternShapeMatch.Special(shMatch.getSecond(), shMatch.getFirst());
        }

        return new PatternShapeMatch.Nothing();
    }

    @Nullable
    public static HexPattern getCanonicalStrokesPerWorld(ResourceKey<ActionRegistryEntry> key, ServerLevel overworld) {
        var perWorldPatterns = ScrungledPatternsSave.open(overworld);

        var pair = perWorldPatterns.lookupReverse(key);
        if (pair == null) return null;

        var sig = pair.getFirst();
        var entry = pair.getSecond();
        return HexPattern.fromAngles(sig, entry.canonicalStartDir());
    }

    /**
     * Get the IDs of all the patterns marked as per-world
     */
    public static Collection<ResourceKey<ActionRegistryEntry>> getAllPerWorldActions() {
        return PER_WORLD_ACTIONS;
    }

    /**
     * Find a valid alternate drawing for this pattern that won't collide with anything pre-existing
     */
    public static HexPattern scrunglePattern(HexPattern prototype, long seed) {
        return EulerPathFinder.findAltDrawing(prototype, seed, it -> {
            var sig = it.anglesSignature();
            return !NORMAL_ACTION_LOOKUP.containsKey(sig);
        });
    }
}
