package at.petrak.hexcasting.api.block.circle;

import at.petrak.hexcasting.api.circles.Margin;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Block holder of a {@link BlockEntitySidedCircleWidget}.
 */
public class BlockSidedCircleWidget extends Block {
    public static final double THICKNESS = 8;
    public static final VoxelShape AABB_XP = Block.box(0, 0, 0, THICKNESS, 16, 16);
    public static final VoxelShape AABB_XN = Block.box(16 - THICKNESS, 0, 0, 16, 16, 16);
    public static final VoxelShape AABB_YP = Block.box(0, 16 - THICKNESS, 0, 16, 16, 16);
    public static final VoxelShape AABB_YN = Block.box(0, 0, 0, 16, THICKNESS, 16);
    public static final VoxelShape AABB_ZP = Block.box(0, 0, 0, 16, 16, THICKNESS);
    public static final VoxelShape AABB_ZN = Block.box(0, 0, 16 - THICKNESS, 16, 16, 16);

    public static final BooleanProperty ENERGIZED = BooleanProperty.create("energized");
    /**
     * The normal vector of this block
     */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    /**
     * Which margin this block considers to be its "top."
     */
    public static final EnumProperty<Margin> TOP_MARGIN = EnumProperty.create("top_margin", Margin.class);

    public BlockSidedCircleWidget(Properties properties) {
        super(properties);
    }
}
