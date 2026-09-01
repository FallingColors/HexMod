package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockNeuralMesh extends MultifaceBlock {
    public static final MapCodec<BlockNeuralMesh> CODEC = simpleCodec(BlockNeuralMesh::new);
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public MapCodec<BlockNeuralMesh> codec() {
        return CODEC;
    }

    public BlockNeuralMesh(BlockBehaviour.Properties arg) {
        super(arg);
    }

    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }

    @Override
    protected boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return blockPlaceContext.getItemInHand().is(HexItems.NEURAL_MESH.get());
    }
}
