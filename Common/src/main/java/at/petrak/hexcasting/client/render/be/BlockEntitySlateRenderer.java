package at.petrak.hexcasting.client.render.be;

import at.petrak.hexcasting.client.render.WorldlyPatternRenderHelpers;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class BlockEntitySlateRenderer implements BlockEntityRenderer<BlockEntitySlate> {
    public BlockEntitySlateRenderer(BlockEntityRendererProvider.Context ctx) {
        // NO-OP
    }

    @Override
    public void render(BlockEntitySlate tile, float pPartialTick, PoseStack ps,
        MultiBufferSource buffer, int light, int overlay) {
        if (tile.pattern == null)
            return;

        var bs = tile.getBlockState();

        WorldlyPatternRenderHelpers.renderPatternForSlate(tile, tile.pattern, ps, buffer, light, bs);
    }
}