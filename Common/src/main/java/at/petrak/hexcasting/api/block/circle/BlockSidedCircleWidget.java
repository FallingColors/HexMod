package at.petrak.hexcasting.api.block.circle;

import at.petrak.hexcasting.api.circles.Margin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Block holder of a {@link BlockEntitySidedCircleWidget}.
 */
abstract public class BlockSidedCircleWidget extends Block implements EntityBlock {
    public static final double THICKNESS = 8;
    public static final VoxelShape AABB_XP = Block.box(16 - THICKNESS, 0, 0, 16, 16, 16);
    public static final VoxelShape AABB_XN = Block.box(0, 0, 0, THICKNESS, 16, 16);
    public static final VoxelShape AABB_YP = Block.box(0, 0, 0, 16, THICKNESS, 16);
    public static final VoxelShape AABB_YN = Block.box(0, 16 - THICKNESS, 0, 16, 16, 16);
    public static final VoxelShape AABB_ZP = Block.box(0, 0, 16 - THICKNESS, 16, 16, 16);
    public static final VoxelShape AABB_ZN = Block.box(0, 0, 0, 16, 16, THICKNESS);

    /**
     * If this has been activated yet by the circle.
     */
    public static final BooleanProperty ENERGIZED = BooleanProperty.create("energized");
    /**
     * The normal vector of this block
     */
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /**
     * Which margin this block considers to be its "top." AKA, its local facing dir.
     */
    public static final EnumProperty<Margin> TOP_MARGIN = EnumProperty.create("top_margin", Margin.class);
    // storing these 4 properties raises the property count to 384, but minecraft noteblocks have like 768
    // I'll be fiiiiiiiiiiiiine
    public static final BooleanProperty TOP_CONNECTS = BooleanProperty.create("top_connects");
    public static final BooleanProperty RIGHT_CONNECTS = BooleanProperty.create("right_connects");
    public static final BooleanProperty BOTTOM_CONNECTS = BooleanProperty.create("bottom_connects");
    public static final BooleanProperty LEFT_CONNECTS = BooleanProperty.create("left_connects");

    public BlockSidedCircleWidget(Properties properties) {
        super(properties);
        var statedef = this.getStateDefinition().any()
            .setValue(ENERGIZED, false)
            .setValue(FACING, Direction.NORTH)
            .setValue(TOP_MARGIN, Margin.TOP);
        for (var margin : Margin.values()) {
            statedef = statedef.setValue(getConnectorProp(margin), false);
        }
        this.registerDefaultState(statedef);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENERGIZED, FACING, TOP_MARGIN);
        for (var margin : Margin.values()) {
            builder.add(getConnectorProp(margin));
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> AABB_YN;
            case UP -> AABB_YP;
            case NORTH -> AABB_ZN;
            case SOUTH -> AABB_ZP;
            case WEST -> AABB_XN;
            case EAST -> AABB_XP;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var normal = ctx.getClickedFace();
        if (ctx.isSecondaryUseActive()) {
            normal = normal.getOpposite();
        }

        var margin = getHoveredMargin(ctx.getClickLocation(), normal);

        return this.defaultBlockState()
            .setValue(FACING, normal)
            .setValue(TOP_MARGIN, margin);
    }

    /**
     * Calculate the little triangle overlay thing IE does.
     */
    public static Margin getHoveredMargin(Vec3 clickPos, Direction normal) {
        // uv cause textures
        double u, v;
        switch (normal.getAxis()) {
            case X -> {
                u = Mth.positiveModulo(clickPos.z * -1 * normal.getAxisDirection().getStep(), 1);
                v = Mth.positiveModulo(clickPos.y, 1);
            }
            case Y -> {
                // North is "up", so +v here ...
                // and u is a quarter-turn to the right, so east.
                u = Mth.positiveModulo(clickPos.x * normal.getAxisDirection().getStep(), 1);
                v = Mth.positiveModulo(clickPos.z * -1 * normal.getAxisDirection().getStep(), 1);
            }
            case Z -> {
                u = Mth.positiveModulo(clickPos.x * normal.getAxisDirection().getStep(), 1);
                v = Mth.positiveModulo(clickPos.y, 1);
            }
            default -> throw new IllegalStateException();
        }
        u -= 0.5;
        v -= 0.5;

        // i have done some futzing around with desmos
        // also i know this is shoddy code, but i've never found a good way to do this matrix of boolean thing,
        // especially when I cant `match` on a tuple of booleans. so i'll just comment.
        // this is true when the point is in an "hourglass", within two triangles radiating up and down...
        var flag1 = Math.abs(u) < Math.abs(v) ? 1 : 0;
        // ... and this is true in the upper-left diagonal half of the coordinate plane
        var flag2 = u < v ? 1 : 0;
        var id = (flag1 << 1) | flag2;
        return switch (id) {
            case 0b00 -> Margin.RIGHT;
            case 0b01 -> Margin.LEFT;
            case 0b10 -> Margin.BOTTOM;
            case 0b11 -> Margin.TOP;
            default -> throw new IllegalStateException();
        };
    }

    public static BooleanProperty getConnectorProp(Margin margin) {
        return switch (margin) {
            case TOP -> TOP_CONNECTS;
            case LEFT -> LEFT_CONNECTS;
            case BOTTOM -> BOTTOM_CONNECTS;
            case RIGHT -> RIGHT_CONNECTS;
        };
    }
}
