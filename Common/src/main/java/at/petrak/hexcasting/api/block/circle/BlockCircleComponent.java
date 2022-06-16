package at.petrak.hexcasting.api.block.circle;

import at.petrak.hexcasting.api.circles.BlockEdge;
import at.petrak.hexcasting.api.circles.FlowUpdate;
import at.petrak.hexcasting.api.circles.ICircleState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.EnumSet;

/**
 * On both the Forge and Fabric sides, the registry will be scanned for all blocks which extend this,
 * and the appropriate cap/CC will be attached.
 */
public interface BlockCircleComponent {
    BooleanProperty ENERGIZED = BooleanProperty.create("energized");

    boolean acceptsFlow(BlockEdge edge, BlockPos pos, BlockState bs, Level world);

    EnumSet<BlockEdge> possibleExitDirs(BlockEdge inputEdge, BlockPos pos, BlockState bs, Level world);

    FlowUpdate onReceiveFlow(BlockEdge inputEdge, BlockPos herePos, BlockState bs, BlockPos senderPos,
        BlockState senderBs, ICircleState state);
}
