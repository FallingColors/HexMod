package at.petrak.hexcasting.api.block.circle;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;

import static at.petrak.hexcasting.api.casting.circles.CircleExecutionState.CACHE;

// Convenience impl of ICircleComponent
public abstract class BlockCircleComponent extends Block implements ICircleComponent {
    public static final BooleanProperty ENERGIZED = BooleanProperty.create("energized");

    public BlockCircleComponent(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockState startEnergized(BlockPos pos, BlockState bs, Level world) {
        var newState = bs.setValue(ENERGIZED, true);
        world.setBlockAndUpdate(pos, newState);

        return newState;
    }

    @Override
    public void destroy(@NotNull LevelAccessor world, @NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        // spaghet
        for (Map.Entry<BlockPos, AABB> entry : CACHE.entrySet()) {
            Vec3 location = blockPos.getCenter();
            if (!entry.getValue().contains(location)) {continue;}
            CACHE.remove(entry.getKey());
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState bs, @Nullable LivingEntity entity, ItemStack stack) {
        for (Map.Entry<BlockPos, AABB> entry : CACHE.entrySet()) {
            Vec3 location = blockPos.getCenter();
            AABB aabb = entry.getValue();
            if (!aabb.contains(location)) {continue;}
            if (aabb.distanceToSqr(location) > 0.250009) {continue;}
            CACHE.remove(entry.getKey());
        }
    }

    @Override
    public boolean isEnergized(BlockPos pos, BlockState bs, Level world) {
        return bs.getValue(ENERGIZED);
    }

    @Override
    public BlockState endEnergized(BlockPos pos, BlockState bs, Level world) {
        var newState = bs.setValue(ENERGIZED, false);
        world.setBlockAndUpdate(pos, newState);
        return newState;
    }

    /**
     * Which direction points "up" or "out" for this block?
     * This is used for {@link ICircleComponent#canEnterFromDirection(Direction, BlockPos, BlockState, ServerLevel)}
     * as well as particles.
     */
    public Direction normalDir(BlockPos pos, BlockState bs, Level world) {
        return normalDir(pos, bs, world, 16);
    }

    abstract public Direction normalDir(BlockPos pos, BlockState bs, Level world, int recursionLeft);

    public static Direction normalDirOfOther(BlockPos other, Level world, int recursionLeft) {
        if (recursionLeft <= 0) {
            return Direction.UP;
        }

        var stateThere = world.getBlockState(other);
        if (stateThere.getBlock() instanceof BlockCircleComponent bcc) {
            return bcc.normalDir(other, stateThere, world, recursionLeft - 1);
        } else {
            return Direction.UP;
        }
    }

    /**
     * How many blocks in the {@link BlockCircleComponent#normalDir(BlockPos, BlockState, Level)} from the center
     * particles should be spawned in
     */
    abstract public float particleHeight(BlockPos pos, BlockState bs, Level world);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(ENERGIZED);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return pState.getValue(ENERGIZED) ? 15 : 0;
    }

    public static BlockState placeStateDirAndSneak(BlockState stock, BlockPlaceContext ctx) {
        var dir = ctx.getNearestLookingDirection();
        if (ctx.getPlayer() != null && ctx.getPlayer().isDiscrete()) {
            dir = dir.getOpposite();
        }
        return stock.setValue(BlockStateProperties.FACING, dir);
    }
}
