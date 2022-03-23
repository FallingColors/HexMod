package at.petrak.hexcasting.client.entity;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.entities.EntityWallScroll;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class WallScrollRenderer extends EntityRenderer<EntityWallScroll> {
    private static final ResourceLocation PRISTINE_BG = new ResourceLocation(HexMod.MOD_ID,
        "textures/entity/scroll.png");
    private static final ResourceLocation ANCIENT_BG = new ResourceLocation(HexMod.MOD_ID,
        "textures/entity/scroll_ancient.png");
    private static final ResourceLocation WHITE = new ResourceLocation(HexMod.MOD_ID,
        "textures/entity/white.png");

    public WallScrollRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    // I do as the PaintingRenderer guides
    @Override
    public void render(EntityWallScroll wallScroll, float yaw, float partialTicks, PoseStack ps,
        MultiBufferSource bufSource, int packedLight) {


        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        ps.pushPose();

        ps.mulPose(Vector3f.YP.rotationDegrees(180f - yaw));
        ps.mulPose(Vector3f.ZP.rotationDegrees(180f));

        int light = LevelRenderer.getLightColor(wallScroll.level,
            wallScroll.getPos().relative(wallScroll.getDirection()));

        {
            ps.pushPose();
            // X is right, Y is down, Z is *in*
            // Our origin will be the lower-left corner of the scroll touching the wall
            // (so it has "negative" thickness)
            ps.translate(-1.5, -1.5, 1f / 32f);

            float dx = 3f, dy = 3f, dz = -1f / 16f;
            float margin = 1f / 48f;
            var last = ps.last();
            var mat = last.pose();
            var norm = last.normal();

            var verts = bufSource.getBuffer(RenderType.entityCutout(this.getTextureLocation(wallScroll)));
            // Remember: CCW
            // Front face
            vertex(mat, norm, light, verts, 0, 0, dz, 0, 0, 0, 0, -1);
            vertex(mat, norm, light, verts, 0, dy, dz, 0, 1, 0, 0, -1);
            vertex(mat, norm, light, verts, dx, dy, dz, 1, 1, 0, 0, -1);
            vertex(mat, norm, light, verts, dx, 0, dz, 1, 0, 0, 0, -1);
            // Back face
            vertex(mat, norm, light, verts, 0, 0, 0, 0, 0, 0, 0, 1);
            vertex(mat, norm, light, verts, dx, 0, 0, 1, 0, 0, 0, 1);
            vertex(mat, norm, light, verts, dx, dy, 0, 1, 1, 0, 0, 1);
            vertex(mat, norm, light, verts, 0, dy, 0, 0, 1, 0, 0, 1);
            // Top face
            vertex(mat, norm, light, verts, 0, 0, 0, 0, 0, 0, -1, 0);
            vertex(mat, norm, light, verts, 0, 0, dz, 0, margin, 0, -1, 0);
            vertex(mat, norm, light, verts, dx, 0, dz, 1, margin, 0, -1, 0);
            vertex(mat, norm, light, verts, dx, 0, 0, 1, 0, 0, -1, 0);
            // Left face
            vertex(mat, norm, light, verts, 0, 0, 0, 0, 0, -1, 0, 0);
            vertex(mat, norm, light, verts, 0, dy, 0, 0, 1, -1, 0, 0);
            vertex(mat, norm, light, verts, 0, dy, dz, margin, 1, -1, 0, 0);
            vertex(mat, norm, light, verts, 0, 0, dz, margin, 0, -1, 0, 0);
            // Right face
            vertex(mat, norm, light, verts, dx, 0, dz, 1 - margin, 0, 1, 0, 0);
            vertex(mat, norm, light, verts, dx, dy, dz, 1 - margin, 1, 1, 0, 0);
            vertex(mat, norm, light, verts, dx, dy, 0, 1, 1, 1, 0, 0);
            vertex(mat, norm, light, verts, dx, 0, 0, 1, 0, 1, 0, 0);
            // Bottom face
            vertex(mat, norm, light, verts, 0, dy, dz, 0, 1 - margin, 0, 1, 0);
            vertex(mat, norm, light, verts, 0, dy, 0, 0, 1, 0, 1, 0);
            vertex(mat, norm, light, verts, dx, dy, 0, 1, 1, 0, 1, 0);
            vertex(mat, norm, light, verts, dx, dy, dz, 1, 1 - margin, 0, 1, 0);

            ps.popPose();
        }

        if (wallScroll.zappyPoints != null) {
            ps.pushPose();

            ps.mulPose(Vector3f.YP.rotationDegrees(180f));
            ps.translate(0, 0, 1.1f / 16f);
            float scale = 1f / 40f;
            ps.scale(scale, scale, scale);

            var last = ps.last();
            var mat = last.pose();
            var norm = last.normal();
            var outer = 0xff_d2c8c8;
            var inner = 0xc8_322b33;
            var verts = bufSource.getBuffer(RenderType.entityCutout(WHITE));
            theCoolerDrawLineSeq(mat, norm, light, verts, wallScroll.zappyPoints, 5, outer);
            ps.translate(0, 0, 0.01);
            theCoolerDrawLineSeq(mat, norm, light, verts, wallScroll.zappyPoints, 2, inner);

            ps.popPose();
        }

        ps.popPose();
        super.render(wallScroll, yaw, partialTicks, ps, bufSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityWallScroll wallScroll) {
        if (wallScroll.isAncient) {
            return ANCIENT_BG;
        } else {
            return PRISTINE_BG;
        }
    }

    private static void vertex(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts, float x, float y,
        float z, float u,
        float v, float nx, float ny, float nz) {
        verts.vertex(mat, x, y, z)
            .color(0xffffffff)
            .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
            .normal(normal, nx, ny, nz)
            .endVertex();
    }

    private static void vertexCol(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts, int col, float x,
        float y) {
        verts.vertex(mat, -x, y, 0)
            .color(col)
            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
            .normal(normal, 0, 0, 1)
            .endVertex();
    }

    private static void theCoolerDrawLineSeq(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts,
        List<Vec2> points, float width, int color
    ) {
        if (points.size() <= 1) {
            return;
        }

        float prevXHi, prevYHi, prevXLo, prevYLo;
        {
            var p1 = points.get(0);
            var p2 = points.get(1);

            var dx = p2.x - p1.x;
            var dy = p2.y - p1.y;
            var nx = -dy;
            var ny = dx;
            var tlen = Mth.sqrt(nx * nx + ny * ny) / (width * 0.5f);
            var tx = nx / tlen;
            var ty = ny / tlen;

            prevXHi = p1.x - tx;
            prevYHi = p1.y - ty;
            prevXLo = p1.x + tx;
            prevYLo = p1.y + ty;
        }

        for (var i = 0; i < points.size() - 1; i++) {
            var p1 = points.get(i);
            var p2 = points.get(i + 1);

            var dx = p2.x - p1.x;
            var dy = p2.y - p1.y;
            var nx = -dy;
            var ny = dx;
            var tlen = Mth.sqrt(nx * nx + ny * ny) / (width * 0.5f);
            var tx = nx / tlen;
            var ty = ny / tlen;

            var xHi = p2.x - tx;
            var yHi = p2.y - ty;
            var xLo = p2.x + tx;
            var yLo = p2.y + ty;
            vertexCol(mat, normal, light, verts, color, prevXHi, prevYHi);
            vertexCol(mat, normal, light, verts, color, prevXLo, prevYLo);
            vertexCol(mat, normal, light, verts, color, xLo, yLo);
            vertexCol(mat, normal, light, verts, color, xHi, yHi);

            prevXHi = xHi;
            prevYHi = yHi;
            prevXLo = xLo;
            prevYLo = yLo;
        }
    }
}
