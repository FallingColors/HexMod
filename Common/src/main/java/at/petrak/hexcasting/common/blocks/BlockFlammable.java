package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.annotations.SoftImplement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Does absolutely nothing on Fabric; the flammable block registry is for that.
 */
public class BlockFlammable extends Block {
    public final int burn, spread;

    public BlockFlammable(Properties $$0, int burn, int spread) {
        super($$0);
        this.burn = burn;
        this.spread = spread;
    }

    @SoftImplement("forge")
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @SoftImplement("forge")
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return burn;
    }

    @SoftImplement("forge")
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return spread;
    }
}
