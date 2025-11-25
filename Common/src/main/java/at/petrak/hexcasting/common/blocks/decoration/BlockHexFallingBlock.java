package at.petrak.hexcasting.common.blocks.decoration;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockHexFallingBlock extends FallingBlock {
    public BlockHexFallingBlock(Properties props) {
        super(props);
    }
    public static final MapCodec<BlockHexFallingBlock> CODEC = BlockBehaviour.simpleCodec(BlockHexFallingBlock::new);
    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }
}
