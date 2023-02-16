package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.common.blocks.entity.BlockEntityQuenchedAllay;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class BlockQuenchedAllay extends Block implements EntityBlock {
    public static final int VARIANTS = 4;

    public static ResourceLocation GASLIGHTING_PRED = modLoc("variant");

    public BlockQuenchedAllay(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityQuenchedAllay(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
