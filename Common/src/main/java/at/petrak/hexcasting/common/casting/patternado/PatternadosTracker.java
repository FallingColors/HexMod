package at.petrak.hexcasting.common.casting.patternado;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps track of the patterns swirling around each player
 */
public class PatternadosTracker {
    // god i'm really calling this a patternado what is wrong with me
    private static final Map<UUID, List<PatternadoPatInstance>> PATTERNADOS = new HashMap<>();

    private static final int MAX_IDX = 300;
    private static int CURRENT_IDX = 0;


}
