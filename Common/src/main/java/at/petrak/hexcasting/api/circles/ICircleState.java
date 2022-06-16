package at.petrak.hexcasting.api.circles;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.Set;

/**
 * Getter for lots of information about the currently operating circle's state.
 */
public interface ICircleState {
    BlockPos impetusPos();

    Pair<BlockPos, BlockPos> circleBounds();

    /**
     * Block positions the circle has floodfilled for ahead of time, when starting the circle.
     * <p>
     * See {@link ICircleState#isPosValid} for more info.
     */
    Set<BlockPos> allScannedPositions();

    /**
     * Whether this block position has been considered for being in the circle.
     * <p>
     * Anything this returns {@code false} for will fail the circle if it tries to output there.
     * This will also confuse the player, a lot, as the intention is that once a ritual starts successfully either
     * it encounters a normal mishap or finishes. So please code your circle components well.
     */
    default boolean isPosValid(BlockPos pos) {
        return allScannedPositions().contains(pos);
    }

    /**
     * A reference to the level, cause it's handy.
     */
    ServerLevel getLevel();
}
