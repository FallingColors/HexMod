package at.petrak.hexcasting.api.block.circle;

import at.petrak.hexcasting.api.block.HexBlockEntity;
import at.petrak.hexcasting.api.circles.BlockEdge;
import at.petrak.hexcasting.api.circles.FlowUpdate;
import at.petrak.hexcasting.api.circles.ICircleState;
import at.petrak.hexcasting.api.circles.Margin;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

/**
 * Helper class for halfslab-sized blocks that sit on the sides of blocks.
 * <p>
 * On both the Forge and Fabric sides, the registry will be scanned for all blocks which extend this,
 * and the appropriate cap/CC will be attached.
 * <p>
 * No, there isn't a more generic version for you to use. Why? In case you don't want to be locked into
 * using {@link HexBlockEntity}.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class BlockEntitySidedCircleWidget extends HexBlockEntity {
    public BlockEntitySidedCircleWidget(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    // "Overrides."
    // The BlockEdge is transformed into a Margin in the forge/fabric implers
    public abstract boolean acceptsFlow(Margin margin);

    public abstract EnumSet<BlockEdge> possibleExitDirs(Margin inputMargin);

    public abstract FlowUpdate onReceiveFlow(Margin inputMargin, BlockEntity sender, ICircleState state);

    /**
     * Convert the given edge to a margin in the block's local marginspace (ie, post-transformation),
     * or {@code null} if it's not an edge this block touches.
     */
    @Nullable
    public Margin getMargin(BlockEdge edge) {
        var bs = this.getBlockState();
        var hereNormal = bs.getValue(BlockSidedCircleWidget.FACING);
        var otherNormal = edge.getOtherNormal(hereNormal);
        if (otherNormal == null) {
            return null;
        }

        var absoluteMargin = Margin.fromNormalAndDir(hereNormal, otherNormal);
        var topMargin = bs.getValue(BlockSidedCircleWidget.TOP_MARGIN);
        return absoluteMargin.transform(topMargin);
    }
}
