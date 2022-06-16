package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.circles.BlockEdge;
import at.petrak.hexcasting.api.circles.FlowUpdate;
import at.petrak.hexcasting.api.circles.ICircleState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/**
 * Additional data attached to blocks that make them work as components in a Spell Circle.
 * <p>
 * See also {@link at.petrak.hexcasting.api.circles api.circles}.
 */
public interface ADCircleComponent {
    /**
     * Whether this block accepts flow from the given edge.
     * <p>
     * This should be immutably based on the block's state. A single state would output {@code true} for
     * all the edges it is connected to, for example.
     */
    boolean acceptsFlow(BlockEdge edge, BlockPos pos, BlockState bs, ICircleState state);

    /**
     * Directions this block <i>can</i> disperse flow to.
     * <p>
     * This should be immutably based on the block's state. A directrix would output both of the edges it can
     * exit from, for example.
     */
    EnumSet<BlockEdge> possibleExitDirs(BlockEdge inputEdge, BlockPos pos, BlockState bs, Level world);

    /**
     * What this block should do upon receiving the flow.
     */
    FlowUpdate onReceiveFlow(BlockEdge inputEdge, BlockPos herePos, BlockState bs, BlockPos senderPos,
        BlockState senderBs, ICircleState state);
}
