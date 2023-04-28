package at.petrak.hexcasting.common.casting.patternado;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The patterns swirling around a particular player.
 * <p>
 * The canonical version of each player's patternado is stored on the server. When a new pattern is added on the server,
 * the client hears about the addition. Clients remove patterns once they've run out of time themselves.
 * <p>
 * Also there's something about loading a brand new patternado when meeting a player for the first time
 */
public class Patternado {
    private static final int MAX_IDX = 300;

    private static final String TAG_PATS = "patterns",
        TAG_IDX_COUNTER = "idx_counter";

    private Player owner;
    private Deque<PatternadoPatInstance> pats;
    private int idxCounter;

    public Patternado(Player owner) {
        this.owner = owner;
        this.pats = new ArrayDeque<>();
        this.idxCounter = 0;
    }

    public void addPattern(HexPattern pattern, int lifetime) {

    }
}
