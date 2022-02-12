package at.petrak.hexcasting.common.blocks;

import at.petrak.hexcasting.hexmath.HexDir;
import at.petrak.hexcasting.hexmath.HexPattern;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Random;

public class BlockEntitySlate extends BlockEntity {
    private static final Random RANDOM = new Random();
    private HexPattern pattern = new HexPattern(HexDir.EAST, new ArrayList<>());

    public static final String TAG_PATTERN = "pattern";

    public BlockEntitySlate(BlockPos pos, BlockState state) {
        super(HexBlocks.SLATE_TILE.get(), pos, state);
    }

    public static class Renderer implements BlockEntityRenderer<BlockEntity> {
        private final BlockRenderDispatcher dispatcher;

        public Renderer(BlockEntityRendererProvider.Context ctx) {
            this.dispatcher = ctx.getBlockRenderDispatcher();
        }

        @Override
        public void render(BlockEntity maybeTile, float pPartialTick, PoseStack ps,
            MultiBufferSource buffer, int pPackedLight, int pPackedOverlay) {
            

            if (!(maybeTile instanceof BlockEntitySlate tile)) {
                return;
            }
        }
    }
}
