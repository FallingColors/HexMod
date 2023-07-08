package at.petrak.hexcasting.client.render.be;

import at.petrak.hexcasting.client.RegisterClientStuff;
import at.petrak.hexcasting.client.render.GaslightingTracker;
import at.petrak.hexcasting.common.blocks.BlockQuenchedAllay;
import at.petrak.hexcasting.common.blocks.entity.BlockEntityQuenchedAllay;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.AABB;

// TODO: this doesn't cover the block being *behind* something. Is it possible to cleanly do that?
// it would probably require some depth-texture bullshit that I don't want to worry about
public class BlockEntityQuenchedAllayRenderer implements BlockEntityRenderer<BlockEntityQuenchedAllay> {
    private final BlockEntityRendererProvider.Context ctx;

    public BlockEntityQuenchedAllayRenderer(BlockEntityRendererProvider.Context ctx) {
        this.ctx = ctx;
    }

    private static void doRender(BlockQuenchedAllay block, BlockRenderDispatcher dispatcher, PoseStack ps, MultiBufferSource bufSource,
        int packedLight, int packedOverlay) {
        var buffer = bufSource.getBuffer(RenderType.translucent());
        var pose = ps.last();

        var idx = Math.abs(GaslightingTracker.getGaslightingAmount() % BlockQuenchedAllay.VARIANTS);
        var model = RegisterClientStuff.QUENCHED_ALLAY_VARIANTS.get(BuiltInRegistries.BLOCK.getKey(block)).get(idx);

        dispatcher.getModelRenderer().renderModel(pose, buffer, null, model, 1f, 1f, 1f, packedLight, packedOverlay);
    }

    @Override
    public void render(BlockEntityQuenchedAllay blockEntity, float partialTick, PoseStack poseStack,
        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // https://github.com/MinecraftForge/MinecraftForge/blob/79dfdb0ace9694f9dd0f1d9e8c5c24a0ae2d77f8/patches/minecraft/net/minecraft/client/renderer/LevelRenderer.java.patch#L68
        // Forge fixes BEs rendering offscreen; Fabric doesn't!
        // So we do a special check on Fabric only
        var pos = blockEntity.getBlockPos();
        var aabb = new AABB(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
        if (IClientXplatAbstractions.INSTANCE.fabricAdditionalQuenchFrustumCheck(aabb)) {
            doRender((BlockQuenchedAllay) blockEntity.getBlockState().getBlock(), this.ctx.getBlockRenderDispatcher(), poseStack, bufferSource, packedLight, packedOverlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityQuenchedAllay blockEntity) {
        return false;
    }

}
