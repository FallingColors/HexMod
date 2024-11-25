package at.petrak.hexcasting.client.render.be;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.render.WorldlyPatternRenderHelpers;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class BlockEntityAkashicBookshelfRenderer implements BlockEntityRenderer<BlockEntityAkashicBookshelf> {
    public BlockEntityAkashicBookshelfRenderer(BlockEntityRendererProvider.Context ctx) {
        // NO-OP
    }

    @Override
    public void render(BlockEntityAkashicBookshelf tile, float pPartialTick, PoseStack ps,
        MultiBufferSource buffer, int light, int overlay) {
        HexPattern pattern = tile.getPattern();
        if (pattern == null) {
            return;
        }

        var bs = tile.getBlockState();

        WorldlyPatternRenderHelpers.renderPatternForAkashicBookshelf(tile, pattern, ps, buffer, light, bs);
    }
}
