package at.petrak.hexcasting.client.entity;

import at.petrak.hexcasting.client.render.WorldlyPatternRenderHelpers;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class WallScrollRenderer extends EntityRenderer<EntityWallScroll> {
    private static final ResourceLocation PRISTINE_BG_LARGE = modLoc("textures/entity/scroll_large.png");
    private static final ResourceLocation PRISTINE_BG_MEDIUM = modLoc("textures/entity/scroll_medium.png");
    private static final ResourceLocation PRISTINE_BG_SMOL = modLoc("textures/block/scroll_paper.png");
    private static final ResourceLocation ANCIENT_BG_LARGE = modLoc("textures/entity/scroll_ancient_large.png");
    private static final ResourceLocation ANCIENT_BG_MEDIUM = modLoc("textures/entity/scroll_ancient_medium.png");
    private static final ResourceLocation ANCIENT_BG_SMOL = modLoc("textures/block/ancient_scroll_paper.png");

    public WallScrollRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    // I do as the PaintingRenderer guides
    @Override
    public void render(EntityWallScroll wallScroll, float yaw, float partialTicks, PoseStack ps,
        MultiBufferSource bufSource, int packedLight) {

        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        ps.pushPose();

        ps.mulPose(Axis.YP.rotationDegrees(180f - yaw));
        ps.mulPose(Axis.ZP.rotationDegrees(180f));

        int light = LevelRenderer.getLightColor(wallScroll.level(), wallScroll.getPos());

        {
            ps.pushPose();
            // X is right, Y is down, Z is *in*
            // Our origin will be the lower-left corner of the scroll touching the wall
            // (so it has "negative" thickness)
            ps.translate(-wallScroll.blockSize / 2f, -wallScroll.blockSize / 2f, 1f / 32f);

            float dx = wallScroll.blockSize, dy = wallScroll.blockSize, dz = -1f / 16f;
            float margin = 1f / 48f;
            var last = ps.last();
            var mat = last.pose();

            RenderType layer = RenderType.entityCutout(this.getTextureLocation(wallScroll));

            var verts = bufSource.getBuffer(layer);
            // Remember: CCW
            // Front face
            vertex(mat, last, light, verts, 0, 0, dz, 0, 0, 0, 0, -1);
            vertex(mat, last, light, verts, 0, dy, dz, 0, 1, 0, 0, -1);
            vertex(mat, last, light, verts, dx, dy, dz, 1, 1, 0, 0, -1);
            vertex(mat, last, light, verts, dx, 0, dz, 1, 0, 0, 0, -1);
            // Back face
            vertex(mat, last, light, verts, 0, 0, 0, 0, 0, 0, 0, 1);
            vertex(mat, last, light, verts, dx, 0, 0, 1, 0, 0, 0, 1);
            vertex(mat, last, light, verts, dx, dy, 0, 1, 1, 0, 0, 1);
            vertex(mat, last, light, verts, 0, dy, 0, 0, 1, 0, 0, 1);
            // Top face
            vertex(mat, last, light, verts, 0, 0, 0, 0, 0, 0, -1, 0);
            vertex(mat, last, light, verts, 0, 0, dz, 0, margin, 0, -1, 0);
            vertex(mat, last, light, verts, dx, 0, dz, 1, margin, 0, -1, 0);
            vertex(mat, last, light, verts, dx, 0, 0, 1, 0, 0, -1, 0);
            // Left face
            vertex(mat, last, light, verts, 0, 0, 0, 0, 0, -1, 0, 0);
            vertex(mat, last, light, verts, 0, dy, 0, 0, 1, -1, 0, 0);
            vertex(mat, last, light, verts, 0, dy, dz, margin, 1, -1, 0, 0);
            vertex(mat, last, light, verts, 0, 0, dz, margin, 0, -1, 0, 0);
            // Right face
            vertex(mat, last, light, verts, dx, 0, dz, 1 - margin, 0, 1, 0, 0);
            vertex(mat, last, light, verts, dx, dy, dz, 1 - margin, 1, 1, 0, 0);
            vertex(mat, last, light, verts, dx, dy, 0, 1, 1, 1, 0, 0);
            vertex(mat, last, light, verts, dx, 0, 0, 1, 0, 1, 0, 0);
            // Bottom face
            vertex(mat, last, light, verts, 0, dy, dz, 0, 1 - margin, 0, 1, 0);
            vertex(mat, last, light, verts, 0, dy, 0, 0, 1, 0, 1, 0);
            vertex(mat, last, light, verts, dx, dy, 0, 1, 1, 0, 1, 0);
            vertex(mat, last, light, verts, dx, dy, dz, 1, 1 - margin, 0, 1, 0);

            ps.popPose();

            if(wallScroll.pattern != null)
                WorldlyPatternRenderHelpers.renderPatternForScroll(wallScroll.pattern, wallScroll, ps, bufSource, light, wallScroll.blockSize, wallScroll.getShowsStrokeOrder());
        }

        ps.popPose();
        super.render(wallScroll, yaw, partialTicks, ps, bufSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityWallScroll wallScroll) {
        if (wallScroll.isAncient) {
            if (wallScroll.blockSize <= 1) {
                return ANCIENT_BG_SMOL;
            } else if (wallScroll.blockSize == 2) {
                return ANCIENT_BG_MEDIUM;
            } else {
                return ANCIENT_BG_LARGE;
            }
        } else {
            if (wallScroll.blockSize <= 1) {
                return PRISTINE_BG_SMOL;
            } else if (wallScroll.blockSize == 2) {
                return PRISTINE_BG_MEDIUM;
            } else {
                return PRISTINE_BG_LARGE;
            }
        }
    }

    private static void vertex(Matrix4f mat, PoseStack.Pose last, int light, VertexConsumer verts, float x, float y,
                               float z, float u,
                               float v, float nx, float ny, float nz) {
        verts.addVertex(mat, x, y, z)
                .setColor(0xffffffff)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(last, nx, ny, nz);
    }
}
