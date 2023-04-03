package at.petrak.hexcasting.api.casting.circles;

import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;

import java.util.EnumSet;
import java.util.List;

/**
 * Implement this on a block to make circles interact with it.
 * <p>
 * This is its own interface so you can have your blocks subclass something else, and to avoid enormous
 * files. The mod doesn't check for the interface on anything but blocks.
 */
public interface ICircleComponent {
    /**
     * The heart of the interface! Functionally modify the casting environment.
     * <p>
     * With the new update you can have the side effects happen inline. In fact, you have to have the side effects
     * happen inline.
     * <p>
     * Also, return a list of directions that the control flow can exit this block in.
     * The circle environment will mishap if not exactly 1 of the returned directions can be accepted from.
     */
    ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos,
        BlockState bs, ServerLevel world);

    /**
     * Can this component get transferred to from a block coming in from that direction, with the given normal?
     */
    @Contract(pure = true)
    boolean canEnterFromDirection(Direction enterDir, BlockPos pos, BlockState bs, ServerLevel world);

    /**
     * This determines the directions the control flow <em>can</em> exit from. It's called at the beginning of execution
     * to see if the circle actually forms a loop.
     * <p>
     * For most blocks, this should be the same as returned from {@link ICircleComponent#acceptControlFlow}
     * Things like directrices might return otherwise. Whatever is returned when controlling flow must be a subset of
     * this set.
     */
    @Contract(pure = true)
    EnumSet<Direction> possibleExitDirections(BlockPos pos, BlockState bs, Level world);
    
    /**
     * Given the current position and a direction, return a pair of the new position after a step
     * in that direction, along with the direction (this is a helper function for creating
     * {@link ICircleComponent.ControlFlow}s.
     */
    @Contract(pure = true)
    default Pair<BlockPos, Direction> exitPositionFromDirection(BlockPos pos, Direction dir) {
        return Pair.of(new BlockPos(dir.getStepX(), dir.getStepY(), dir.getStepZ()), dir);
    }
    
    /**
     * Start the {@link ICircleComponent} at the given position glowing. Returns the new state
     * of the given block.
     * // TODO: determine if this should just be in {@link ICircleComponent#acceptControlFlow(CastingImage, CircleCastEnv, Direction, BlockPos, BlockState, ServerLevel)}.
     */
    BlockState startEnergized(BlockPos pos, BlockState bs, Level world);
    
    /**
     * End the {@link ICircleComponent} at the given position glowing. Returns the new state of
     * the given block.
     */
    BlockState endEnergized(BlockPos pos, BlockState bs, Level world);

    abstract sealed class ControlFlow {
        public static final class Continue extends ControlFlow {
            public final CastingImage update;
            public final List<Pair<BlockPos, Direction>> exits;

            public Continue(CastingImage update, List<Pair<BlockPos, Direction>> exits) {
                this.update = update;
                this.exits = exits;
            }
        }

        public static final class Stop extends ControlFlow {
            public Stop() {
            }
        }
    }
}
