package at.petrak.hexcasting.client.render.patternado;

import at.petrak.hexcasting.common.casting.patternado.PatternadoPatInstance;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Keeps track of the patterns swirling around each player
 */
public class PatternadosTracker {
    // god i'm really calling this a patternado what is wrong with me
    private static final Map<UUID, PlayerPats> PATTERNADOS = new HashMap<>();

    /**
     * Get notified of a new pattern around a player.
     */
    public static void getNewPat(UUID owner, PatternadoPatInstance newPat) {
        var nado = PATTERNADOS.get(owner);
        if (nado == null) {
            nado = PATTERNADOS.put(owner, PlayerPats.empty());
        }
        nado.pats.put(newPat.getUuid(), newPat);
    }

    /**
     * Remove a pattern by its uuid. (More accurately, enqueue it for being removed after ticking)
     */
    public static void removePat(UUID owner, UUID pattern) {
        var nado = PATTERNADOS.get(owner);
        if (nado == null) {
            return;
        }
        nado.pats.remove(pattern);
    }

    /**
     * Load a player fresh.
     */
    public void clobberPatterns(UUID owner, List<PatternadoPatInstance> pats) {
        PATTERNADOS.put(owner, PlayerPats.newFromList(pats));
    }

    public static @Nullable Collection<PatternadoPatInstance> getPatterns(UUID owner) {
        var pats = PATTERNADOS.get(owner);
        if (pats == null) {
            return null;
        }
        return pats.pats.values();
    }

    // TODO: this might cause a memory leak if you view tons and tons of players.
    // Perhaps find some way to remove entries from this map if they're not in view?

    public static void tick() {
        for (var nados : PATTERNADOS.values()) {
            var toRemove = new ArrayList<UUID>();
            for (var pat : nados.pats.values()) {
                pat.tick();
                if (pat.getLifetime() == 0) {
                    toRemove.add(pat.getUuid());
                }
            }

            for (var uuid : toRemove) {
                nados.pats.remove(uuid);
            }
        }
    }

    private record PlayerPats(Map<UUID, PatternadoPatInstance> pats) {
        static PlayerPats newFromList(List<PatternadoPatInstance> pats) {
            var map = new HashMap<UUID, PatternadoPatInstance>();
            for (var pat : pats) {
                map.put(pat.getUuid(), pat);
            }
            return new PlayerPats(map);
        }

        static PlayerPats empty() {
            return new PlayerPats(new HashMap<>());
        }
    }
}
