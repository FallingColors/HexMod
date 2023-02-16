package at.petrak.hexcasting.client.be;

import at.petrak.hexcasting.client.RegisterClientStuff;
import at.petrak.hexcasting.common.blocks.BlockQuenchedAllay;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityQuenchedAllay;
import at.petrak.hexcasting.mixin.accessor.client.AccessorBlockEntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

public class BlockEntityQuenchedAllayRenderer implements BlockEntityRenderer<BlockEntityQuenchedAllay> {
    private static int GASLIGHTING_AMOUNT = 0;
    private static boolean HAS_RENDERED_THIS_FRAME = false;

    private final BlockEntityRendererProvider.Context ctx;

    public BlockEntityQuenchedAllayRenderer(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    private static void doRender(BlockRenderDispatcher dispatcher, PoseStack ps, MultiBufferSource bufSource,
        int packedLight, int packedOverlay) {
        HAS_RENDERED_THIS_FRAME = true;

        var buffer = bufSource.getBuffer(RenderType.translucent());
        var pose = ps.last();

        var idx = Math.abs(BlockEntityQuenchedAllayRenderer.GASLIGHTING_AMOUNT % BlockQuenchedAllay.VARIANTS);
        var model = RegisterClientStuff.QUENCHED_ALLAY_VARIANTS.get(idx);

        dispatcher.getModelRenderer().renderModel(pose, buffer, null, model, 1f, 1f, 1f, packedLight, packedOverlay);
    }

    @Override
    public void render(BlockEntityQuenchedAllay blockEntity, float partialTick, PoseStack poseStack,
        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        doRender(this.ctx.getBlockRenderDispatcher(), poseStack, bufferSource, packedLight, packedOverlay);
    }

    public static void postFrameCheckRendered() {
        if (!HAS_RENDERED_THIS_FRAME) {
            GASLIGHTING_AMOUNT += 1;
        }
        HAS_RENDERED_THIS_FRAME = false;
    }

    public static class BWELRWrapper extends BlockEntityWithoutLevelRenderer {
        private final BlockRenderDispatcher dispatcher;

        public BWELRWrapper(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
            super(blockEntityRenderDispatcher, entityModelSet);
            this.dispatcher =
                ((AccessorBlockEntityRenderDispatcher) blockEntityRenderDispatcher).hex$getBlockRenderDispatcher().get();
        }

        @Override
        public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
            doRender(this.dispatcher, poseStack, buffer, packedLight, packedOverlay);
        }
    }
}
