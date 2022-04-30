package at.petrak.hexcasting.forge.xplat.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockBurns extends Block {
    private final int flammability;
    private final int spreadSpeed;

    public BlockBurns(Properties props, int flammability, int spreadSpeed) {
        super(props);
        this.flammability = flammability;
        this.spreadSpeed = spreadSpeed;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return flammability;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return spreadSpeed;
    }
}
