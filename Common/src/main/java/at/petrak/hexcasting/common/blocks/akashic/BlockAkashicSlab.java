package at.petrak.hexcasting.common.blocks.akashic;

import at.petrak.hexcasting.annotations.SoftImplement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockAkashicSlab extends SlabBlock {
    public BlockAkashicSlab(Properties props) {
        super(props);
    }

    @SoftImplement("forge")
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @SoftImplement("forge")
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 20;
    }

    @SoftImplement("forge")
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }
}
