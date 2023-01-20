package at.petrak.hexcasting.common.casting;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.SpecialHandler;
import at.petrak.hexcasting.api.casting.math.EulerPathFinder;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// Now an internal-only class used to do final processing on the registered stuff
public class PatternRegistryManifest {
    // Map actions to their entry except for the per-world ones. Client and server side
    public static final ConcurrentMap<String, ResourceKey<ActionRegistryEntry>> NORMAL_ACTION_LOOKUP =
        new ConcurrentHashMap<>();

    // On the server side, where we know such things, the jumbled patterns
    //
    // SERVER SIDE ONLY
    public static final ConcurrentMap<String, ResourceKey<ActionRegistryEntry>> PER_WORLD_ACTION_LOOKUP =
        new ConcurrentHashMap<>();

    /**
     * Process the registry!
     * <p>
     * Pass null for the OW to signal we're on the client
     */
    public static void processRegistry(@Nullable ServerLevel overworld) {
        ScrungledPatternsSave perWorldPatterns = null;
        if (overworld != null) {
            var ds = overworld.getDataStorage();
            perWorldPatterns = ds.computeIfAbsent(ScrungledPatternsSave::load,
                ScrungledPatternsSave::create,
                TAG_SAVED_DATA);
        }

        var postCalculationNeeders = new ArrayList<ResourceKey<ActionRegistryEntry>>();

        var registry = IXplatAbstractions.INSTANCE.getActionRegistry();
        for (var key : registry.registryKeySet()) {
            var entry = registry.get(key);
            if (registry.getHolderOrThrow(key).is(HexTags.Actions.PER_WORLD_PATTERN)) {
                // Then we need to create this only on the server, gulp
                if (perWorldPatterns != null) {
                    var precalced = perWorldPatterns.lookup.get(entry.prototype().anglesSignature());
                    if (precalced == null) {
                        postCalculationNeeders.add(key);
                        perWorldPatterns.setDirty();
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
                var entry = registry.get(postNeederKey);
                var scrungledSig = scrunglePattern(entry.prototype(), overworld.getSeed());
                PER_WORLD_ACTION_LOOKUP.put(scrungledSig, postNeederKey);
            }
        }

        HexAPI.LOGGER.info(("We're on the %s! Loaded %d regular actions, %d per-world actions, and %d special " +
            "handlers").formatted(
            (overworld == null) ? "client" : "server", NORMAL_ACTION_LOOKUP.size(), PER_WORLD_ACTION_LOOKUP.size(),
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
        var ds = overworld.getDataStorage();
        ScrungledPatternsSave perWorldPatterns =
            ds.computeIfAbsent(ScrungledPatternsSave::load, ScrungledPatternsSave::create,
                TAG_SAVED_DATA);
        if (perWorldPatterns.lookup.containsKey(sig)) {
            var key = perWorldPatterns.lookup.get(sig);
            return new PatternShapeMatch.PerWorld(key, true);
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

    /**
     * Maps angle sigs to resource locations and their preferred start dir so we can look them up in the main registry
     * Save this on the world in case the random algorithm changes.
     */
    public static class ScrungledPatternsSave extends SavedData {

        // Maps scrungled signatures to their keys.
        private final Map<String, ResourceKey<ActionRegistryEntry>> lookup;

        public ScrungledPatternsSave(Map<String, ResourceKey<ActionRegistryEntry>> lookup) {
            this.lookup = lookup;
        }


        @Override
        public CompoundTag save(CompoundTag tag) {
            this.lookup.forEach((sig, key) -> tag.putString(sig, key.location().toString()));
            return tag;
        }

        private static ScrungledPatternsSave load(CompoundTag tag) {
            var registryKey = IXplatAbstractions.INSTANCE.getActionRegistry().key();

            var map = new HashMap<String, ResourceKey<ActionRegistryEntry>>();
            for (var sig : tag.getAllKeys()) {
                var keyStr = tag.getString(sig);
                var key = ResourceKey.create(registryKey, new ResourceLocation(keyStr));

                map.put(sig, key);
            }

            return new ScrungledPatternsSave(map);
        }


        public static ScrungledPatternsSave create() {
            var save = new ScrungledPatternsSave(new HashMap<>());
            return save;
        }

    }

    public static final String DATA_VERSION = "0.1.0";
    public static final String TAG_SAVED_DATA = "hexcasting.per-world-patterns." + DATA_VERSION;

    /**
     * Find a valid alternate drawing for this pattern that won't collide with anything pre-existing
     */
    private static String scrunglePattern(HexPattern prototype, long seed) {
        var scrungled = EulerPathFinder.findAltDrawing(prototype, seed, it -> {
            var sig = it.anglesSignature();
            return !NORMAL_ACTION_LOOKUP.containsKey(sig);
        });
        return scrungled.anglesSignature();
    }
}
