package at.petrak.hexcasting.server;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps angle sigs to resource locations and their preferred start dir so we can look them up in the main registry
 * Save this on the world in case the random algorithm changes.
 */
public class ScrungledPatternsSave extends SavedData {
    public static final String DATA_VERSION = "0.1.0";
    public static final String TAG_SAVED_DATA = "hexcasting.per-world-patterns." + DATA_VERSION;
    private static final String TAG_DIR = "startDir";
    private static final String TAG_KEY = "key";

    /**
     * Maps scrungled signatures to their keys.
     */
    private final Map<String, PerWorldEntry> lookup;

    /**
     * Reverse-maps resource keys to their signature; you can use that in {@code lookup}.
     * <p>
     * This way we can look up things if we know their resource key, for commands and such
     */
    private final Map<ResourceKey<ActionRegistryEntry>, String> reverseLookup;

    private ScrungledPatternsSave(Map<String, PerWorldEntry> lookup) {
        this.lookup = lookup;
        this.reverseLookup = new HashMap<>();
        this.lookup.forEach((sig, entry) -> {
            this.reverseLookup.put(entry.key, sig);
        });
    }

    public void insert(HexPattern scrungledPattern, ResourceKey<ActionRegistryEntry> key) {
        this.lookup.put(scrungledPattern.anglesSignature(), new PerWorldEntry(key, scrungledPattern.getStartDir()));
        this.reverseLookup.put(key, scrungledPattern.anglesSignature());
        this.setDirty();
    }

    @Nullable
    public PerWorldEntry lookup(String signature) {
        return this.lookup.get(signature);
    }

    @Nullable
    public Pair<String, PerWorldEntry> lookupReverse(ResourceKey<ActionRegistryEntry> key) {
        var sig = this.reverseLookup.get(key);
        if (sig == null) return null;

        return Pair.of(sig, this.lookup.get(sig));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // We don't save the reverse lookup cause we can reconstruct it when loading.
        this.lookup.forEach((sig, entry) -> {
            var inner = new CompoundTag();
            inner.putByte(TAG_DIR, (byte) entry.canonicalStartDir.ordinal());
            inner.putString(TAG_KEY, entry.key().location().toString());
            tag.put(sig, inner);
        });
        return tag;
    }

    private static ScrungledPatternsSave load(CompoundTag tag) {
        var registryKey = IXplatAbstractions.INSTANCE.getActionRegistry().key();

        var map = new HashMap<String, PerWorldEntry>();
        for (var sig : tag.getAllKeys()) {
            var inner = tag.getCompound(sig);

            var rawDir = inner.getByte(TAG_DIR);
            var rawKey = inner.getString(TAG_KEY);

            var dir = HexDir.values()[rawDir];
            var key = ResourceKey.create(registryKey, new ResourceLocation(rawKey));

            map.put(sig, new PerWorldEntry(key, dir));
        }

        return new ScrungledPatternsSave(map);
    }

    public static ScrungledPatternsSave createFromScratch(long seed) {
        var map = new HashMap<String, PerWorldEntry>();
        for (var key : PatternRegistryManifest.getAllPerWorldActions()) {
            var regiEntry = IXplatAbstractions.INSTANCE.getActionRegistry().get(key);
            var scrungled = PatternRegistryManifest.scrunglePattern(regiEntry.prototype(), seed);
            map.put(scrungled.anglesSignature(), new PerWorldEntry(key, scrungled.getStartDir()));
        }

        return new ScrungledPatternsSave(map);
    }

    public static ScrungledPatternsSave open(ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(
            ScrungledPatternsSave::load,
            () -> ScrungledPatternsSave.createFromScratch(overworld.getSeed()),
            TAG_SAVED_DATA);
    }

    public record PerWorldEntry(ResourceKey<ActionRegistryEntry> key, HexDir canonicalStartDir) {
    }
}
