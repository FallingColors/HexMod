package at.petrak.hexcasting.api;

import at.petrak.hexcasting.api.spell.Action;
import at.petrak.hexcasting.api.spell.math.EulerPathFinder;
import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidPattern;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

public class PatternRegistry {
    private static final ConcurrentMap<ResourceLocation, Action> actionLookup = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedDeque<SpecialHandlerEntry> specialHandlers = new ConcurrentLinkedDeque<>();

    // Map signatures to the "preferred" direction they start in and their operator ID.
    private static final ConcurrentMap<String, RegularEntry> regularPatternLookup =
        new ConcurrentHashMap<>();

    private static final ConcurrentMap<ResourceLocation, PerWorldEntry> perWorldPatternLookup =
        new ConcurrentHashMap<>();

    public static void mapPattern(HexPattern pattern, ResourceLocation id,
        Action action) throws RegisterPatternException {
        mapPattern(pattern, id, action, false);
    }

    /**
     * Associate a given angle signature with a SpellOperator.
     */
    public static void mapPattern(HexPattern pattern, ResourceLocation id, Action action,
        boolean isPerWorld) throws RegisterPatternException {
        if (actionLookup.containsKey(id)) {
            throw new RegisterPatternException("The operator with id `%s` was already registered to: %s", id,
                actionLookup.get(id));
        }

        actionLookup.put(id, action);
        if (isPerWorld) {
            perWorldPatternLookup.put(id, new PerWorldEntry(pattern, id));
        } else {
            regularPatternLookup.put(pattern.anglesSignature(), new RegularEntry(pattern.getStartDir(), id));
        }
    }


    /**
     * Add a special handler, to take an arbitrary pattern and return whatever kind of operator you like.
     */
    public static void addSpecialHandler(SpecialHandlerEntry handler) {
        specialHandlers.add(handler);
    }

    /**
     * Add a special handler, to take an arbitrary pattern and return whatever kind of operator you like.
     */
    public static void addSpecialHandler(ResourceLocation id, SpecialHandler handler) {
        addSpecialHandler(new SpecialHandlerEntry(id, handler));
    }

    /**
     * Internal use only.
     */
    public static Action matchPattern(HexPattern pat, ServerLevel overworld) throws MishapInvalidPattern {
        return matchPatternAndID(pat, overworld).getFirst();
    }

    /**
     * Internal use only.
     */
    public static Pair<Action, ResourceLocation> matchPatternAndID(HexPattern pat,
        ServerLevel overworld) throws MishapInvalidPattern {
        // Pipeline:
        // patterns are registered here every time the game boots
        // when we try to look
        for (var handler : specialHandlers) {
            var op = handler.handler.handlePattern(pat);
            if (op != null) {
                return new Pair<>(op, handler.id);
            }
        }

        // Is it global?
        var sig = pat.anglesSignature();
        if (regularPatternLookup.containsKey(sig)) {
            var it = regularPatternLookup.get(sig);
            if (!actionLookup.containsKey(it.opId)) {
                throw new MishapInvalidPattern();
            }
            var op = actionLookup.get(it.opId);
            return new Pair<>(op, it.opId);
        }

        // Look it up in the world?
        var ds = overworld.getDataStorage();
        Save perWorldPatterns =
            ds.computeIfAbsent(Save::load, () -> Save.create(overworld.getSeed()), TAG_SAVED_DATA);
        if (perWorldPatterns.lookup.containsKey(sig)) {
            var it = perWorldPatterns.lookup.get(sig);
            return new Pair<>(actionLookup.get(it.getFirst()), it.getFirst());
        }

        throw new MishapInvalidPattern();
    }

    /**
     * Internal use only.
     */
    public static Map<String, Pair<ResourceLocation, HexDir>> getPerWorldPatterns(ServerLevel overworld) {
        var ds = overworld.getDataStorage();
        Save perWorldPatterns =
            ds.computeIfAbsent(Save::load, () -> Save.create(overworld.getSeed()), TAG_SAVED_DATA);
        return perWorldPatterns.lookup;
    }

    /**
     * Internal use only.
     */
    public static PatternEntry lookupPattern(ResourceLocation opId) {
        if (perWorldPatternLookup.containsKey(opId)) {
            var it = perWorldPatternLookup.get(opId);
            return new PatternEntry(it.prototype, actionLookup.get(it.opId), true);
        }
        for (var kv : regularPatternLookup.entrySet()) {
            var sig = kv.getKey();
            var entry = kv.getValue();
            if (entry.opId.equals(opId)) {
                var pattern = HexPattern.fromAngles(sig, entry.preferredStart);
                return new PatternEntry(pattern, actionLookup.get(entry.opId), false);
            }
        }

        throw new IllegalArgumentException("could not find a pattern for " + opId);
    }

    /**
     * Internal use only.
     */
    public static Set<ResourceLocation> getAllPerWorldPatternNames() {
        return perWorldPatternLookup.keySet();
    }

    /**
     * Special handling of a pattern. Before checking any of the normal angle-signature based patterns,
     * a given pattern is run by all of these special handlers patterns. If none of them return non-null,
     * then its signature is checked.
     * <p>
     * In the base mod, this is used for number patterns and Bookkeeper's Gambit.
     */
    @FunctionalInterface
    interface SpecialHandler {
        @Nullable Action handlePattern(HexPattern pattern);
    }

    public record SpecialHandlerEntry(ResourceLocation id, SpecialHandler handler) {
    }

    public static class RegisterPatternException extends Exception {
        public RegisterPatternException(String msg, Object... formats) {
            super(String.format(msg, formats));
        }
    }

    private record RegularEntry(HexDir preferredStart, ResourceLocation opId) {
    }

    private record PerWorldEntry(HexPattern prototype, ResourceLocation opId) {
    }

    // Fake class we pretend to use internally
    public record PatternEntry(HexPattern prototype, Action action, boolean isPerWorld) {
    }

    /**
     * Maps angle sigs to resource locations and their preferred start dir so we can look them up in the main registry
     */
    public static class Save extends SavedData {
        private static final String TAG_OP_ID = "op_id";
        private static final String TAG_START_DIR = "start_dir";

        // TODO: this is slightly weird that we save it *on* the world
        // might it be better to recalculate it every time the world loads?
        private Map<String, Pair<ResourceLocation, HexDir>> lookup;

        public Save(
            Map<String, Pair<ResourceLocation, HexDir>> lookup) {
            this.lookup = lookup;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            this.lookup.forEach((sig, rhs) -> {
                var entry = new CompoundTag();
                entry.putString(TAG_OP_ID, sig.toString());
                entry.putInt(TAG_START_DIR, rhs.getSecond().ordinal());
                tag.put(sig, entry);
            });
            return tag;
        }

        static Save load(CompoundTag tag) {
            var map = new HashMap<String, Pair<ResourceLocation, HexDir>>();
            for (var sig : tag.getAllKeys()) {
                var entry = tag.getCompound(sig);
                var opId = ResourceLocation.tryParse(entry.getString(TAG_OP_ID));
                var startDir = HexDir.values()[entry.getInt(TAG_START_DIR)];
                map.put(sig, new Pair<>(opId, startDir));
            }
            return new Save(map);
        }

        public static Save create(long seed) {
            var map = new HashMap<String, Pair<ResourceLocation, HexDir>>();
            PatternRegistry.perWorldPatternLookup.forEach((opId, entry) -> {
                var scrungled = EulerPathFinder.findAltDrawing(entry.prototype, seed);
                map.put(scrungled.anglesSignature(), new Pair<>(opId, scrungled.getStartDir()));
            });
            var save = new Save(map);
            save.setDirty();
            return save;
        }
    }

    public static final String TAG_SAVED_DATA = "hex.per-world-patterns";

    public static String getPatternCountInfo() {
        return String.format(
            "Loaded %d regular patterns, " +
                "%d per-world patterns, and " +
                "%d special handlers.", regularPatternLookup.size(), perWorldPatternLookup.size(),
            specialHandlers.size());
    }
}
