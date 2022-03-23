package at.petrak.hexcasting.client.entity;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public class WallScrollRenderer extends EntityRenderer<EntityWallScroll> {
    private static final ResourceLocation PRISTINE_BG = new ResourceLocation(HexMod.MOD_ID,
        "textures/entity/scroll.png");
    private static final ResourceLocation ANCIENT_BG = new ResourceLocation(HexMod.MOD_ID,
        "textures/entity/scroll_ancient.png");

    public static final ModelResourceLocation PRISTINE_MODEL = new ModelResourceLocation(
        new ResourceLocation(HexMod.MOD_ID, "wall_scroll"), "inventory");
    public static final ModelResourceLocation ANCIENT_MODEL = new ModelResourceLocation(
        new ResourceLocation(HexMod.MOD_ID, "wall_scroll_ancient"), "inventory");

    public WallScrollRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    // I do as the PaintingRenderer guides
    @Override
    public void render(EntityWallScroll wallScroll, float yaw, float partialTicks, PoseStack ps,
        MultiBufferSource buf, int packedLight) {
        var mc = Minecraft.getInstance();
        var blockRenderer = mc.getBlockRenderer();
        var modelManager = mc.getModelManager();

        ps.pushPose();

        ps.mulPose(Vector3f.YP.rotationDegrees(90f - yaw));

        // X is backwards, Y is down, Z is to the right
        ps.translate(-1f / 32f, -0.5, -0.5);

        blockRenderer.getModelRenderer()
            .renderModel(ps.last(), buf.getBuffer(Sheets.cutoutBlockSheet()),
                null,
                modelManager.getModel(wallScroll.isAncient ? ANCIENT_MODEL : PRISTINE_MODEL),
                1f, 1f, 1f, packedLight, OverlayTexture.NO_OVERLAY);


        if (wallScroll.zappyPoints != null) {
            var oldShader = RenderSystem.getShader();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            ps.pushPose();

            ps.mulPose(Vector3f.YP.rotationDegrees(-90f));
            ps.translate(0, 0, 1.01f / 16f);
            float scale = 1f / 16f;
            ps.scale(scale, scale, 1);

            var mat = ps.last().pose();
            var outer = 0xff_d2c8c8;
            var inner = 0xc8_322b33;
            RenderLib.drawLineSeq(mat, wallScroll.zappyPoints, 5f, 0, outer, outer, null);
            RenderLib.drawLineSeq(mat, wallScroll.zappyPoints, 2f, 0, inner, inner);

            ps.popPose();
            RenderSystem.setShader(() -> oldShader);
        }

        ps.popPose();
        super.render(wallScroll, yaw, partialTicks, ps, buf, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityWallScroll wallScroll) {
        if (wallScroll.isAncient) {
            return ANCIENT_BG;
        } else {
            return PRISTINE_BG;
        }
    }
}
