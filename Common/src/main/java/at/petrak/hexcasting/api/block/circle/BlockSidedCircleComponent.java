package at.petrak.hexcasting.api.block.circle;

import at.petrak.hexcasting.api.circles.BlockEdge;
import at.petrak.hexcasting.api.circles.FlowUpdate;
import at.petrak.hexcasting.api.circles.ICircleState;
import at.petrak.hexcasting.api.circles.Margin;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumSet;

/**
 * Helper class for halfslab-sized blocks that sit on the sides of blocks.
 * <p>
 * On both the Forge and Fabric sides, the registry will be scanned for all blocks which extend this,
 * and the appropriate cap/CC will be attached.
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface BlockSidedCircleComponent extends BlockCircleComponent {
    double THICKNESS = 8;
    VoxelShape AABB_XP = Block.box(0, 0, 0, THICKNESS, 16, 16);
    VoxelShape AABB_XN = Block.box(16 - THICKNESS, 0, 0, 16, 16, 16);
    VoxelShape AABB_YP = Block.box(0, 16 - THICKNESS, 0, 16, 16, 16);
    VoxelShape AABB_YN = Block.box(0, 0, 0, 16, THICKNESS, 16);
    VoxelShape AABB_ZP = Block.box(0, 0, 0, 16, 16, THICKNESS);
    VoxelShape AABB_ZN = Block.box(0, 0, 16 - THICKNESS, 16, 16, 16);

    /**
     * The normal vector of this block
     */
    DirectionProperty FACING = BlockStateProperties.FACING;

    /**
     * Which margin this block considers to be its "top."
     */
    EnumProperty<Margin> TOP_MARGIN = EnumProperty.create("top_margin", Margin.class);

    boolean acceptsFlow(Margin margin, BlockPos pos, BlockState bs, Level world);

    EnumSet<BlockEdge> possibleExitDirs(Margin inputMargin, BlockPos pos, BlockState bs, Level world);

    FlowUpdate onReceiveFlow(Margin inputMargin, BlockPos herePos, BlockState bs, BlockPos senderPos,
        BlockState senderBs, ICircleState state);

    /**
     * Convert the given edge to a margin in the block's local marginspace (ie, post-transformation),
     * or {@code null} if it's not an edge this block touches.
     */
    @Nullable
    default Margin getMargin(BlockEdge edge, BlockState bs) {
        var hereNormal = bs.getValue(FACING);
        var otherNormal = edge.getOtherNormal(hereNormal);
        if (otherNormal == null) {
            return null;
        }

        var absoluteMargin = Margin.fromNormalAndDir(hereNormal, otherNormal);
        var topMargin = bs.getValue(TOP_MARGIN);
        return absoluteMargin.transform(topMargin);
    }

    @Override
    default boolean acceptsFlow(BlockEdge edge, BlockPos pos, BlockState bs, Level world) {
        var margin = this.getMargin(edge, bs);
        if (margin == null) {
            return false;
        }
        return this.acceptsFlow(margin, pos, bs, world);
    }

    @Override
    default EnumSet<BlockEdge> possibleExitDirs(BlockEdge inputEdge, BlockPos pos, BlockState bs, Level world) {
        var margin = this.getMargin(inputEdge, bs);
        if (margin == null) {
            return EnumSet.noneOf(BlockEdge.class);
        }

        return this.possibleExitDirs(margin, pos, bs, world);
    }

    @Override
    @Nullable
    default FlowUpdate onReceiveFlow(BlockEdge inputEdge, BlockPos herePos, BlockState bs, BlockPos senderPos,
        BlockState senderBs, ICircleState state) {
        var margin = this.getMargin(inputEdge, bs);
        if (margin == null) {
            return null;
        }

        return this.onReceiveFlow(margin, herePos, bs, senderPos, senderBs, state);
    }
}
