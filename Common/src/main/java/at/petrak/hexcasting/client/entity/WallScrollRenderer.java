package at.petrak.hexcasting.client.entity;

import at.petrak.hexcasting.api.mod.HexConfig;
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

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class WallScrollRenderer extends EntityRenderer<EntityWallScroll> {
    private static final ResourceLocation PRISTINE_BG_LARGE = modLoc("textures/entity/scroll_large.png");
    private static final ResourceLocation PRISTINE_BG_MEDIUM = modLoc("textures/entity/scroll_medium.png");
    private static final ResourceLocation PRISTINE_BG_SMOL = modLoc("textures/block/scroll_paper.png");
    private static final ResourceLocation ANCIENT_BG_LARGE = modLoc("textures/entity/scroll_ancient_large.png");
    private static final ResourceLocation ANCIENT_BG_MEDIUM = modLoc("textures/entity/scroll_ancient_medium.png");
    private static final ResourceLocation ANCIENT_BG_SMOL = modLoc("textures/block/ancient_scroll_paper.png");
    private static final ResourceLocation WHITE = modLoc("textures/entity/white.png");

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

        int light = LevelRenderer.getLightColor(wallScroll.level, wallScroll.getPos());

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
            var points = wallScroll.zappyPoints;
            ps.pushPose();

            ps.mulPose(Vector3f.YP.rotationDegrees(180f));
            ps.translate(0, 0, 1.1f / 16f);
            // make smaller scrolls not be charlie kirk-sized
            // i swear, learning about these functions with asymptotes where slope != 0 is the most useful thing
            // I've ever learned in a math class
            float unCharlieKirk = Mth.sqrt(wallScroll.blockSize * wallScroll.blockSize + 60);
            float scale = 1f / 300f * unCharlieKirk;
            ps.scale(scale, scale, 0.01f);

            var last = ps.last();
            var mat = last.pose();
            var norm = last.normal();
            var outer = 0xff_d2c8c8;
            var inner = 0xc8_322b33;
            var verts = bufSource.getBuffer(RenderType.entityCutout(WHITE));
            theCoolerDrawLineSeq(mat, norm, light, verts, points, wallScroll.blockSize * 5f / 3f, outer);
            ps.translate(0, 0, 0.01);
            theCoolerDrawLineSeq(mat, norm, light, verts, points, wallScroll.blockSize * 2f / 3f, inner);

            if (wallScroll.getShowsStrokeOrder()) {
                var spotFrac = 0.8f * wallScroll.blockSize;

                var animTime = wallScroll.tickCount;
                var pointCircuit =
                    (animTime * HexConfig.client().patternPointSpeedMultiplier()) % (points.size() + 10);
                if (pointCircuit < points.size() - 1) {
                    var pointMacro = Mth.floor(pointCircuit);
                    var pointMicro = pointCircuit - pointMacro;

                    var p1 = points.get(pointMacro);
                    var p2 = points.get((pointMacro + 1) % points.size());
                    var drawPos = new Vec2(
                        (float) (p1.x + (p2.x - p1.x) * pointMicro),
                        (float) (p1.y + (p2.y - p1.y) * pointMicro)
                    );

                    ps.translate(0, 0, 0.01);
                    theCoolerDrawSpot(mat, norm, light, verts, drawPos, 2.6666f / 3f * spotFrac,
                        0xff_cfa0f3);
                    ps.translate(0, 0, 0.01);
                    theCoolerDrawSpot(mat, norm, light, verts, drawPos, 2f / 3f * spotFrac,
                        0xff_8d6acc);
                } else {
                    ps.translate(0, 0, 0.02);
                }

                ps.translate(0, 0, 0.01);
                theCoolerDrawSpot(mat, norm, light, verts, points.get(0), spotFrac, 0xff_4946d3);
                ps.translate(0, 0, 0.01);
                theCoolerDrawSpot(mat, norm, light, verts, points.get(0), 2f / 3f * spotFrac,
                    0xff_5b7bd7);
            }

            ps.popPose();
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

    private static void theCoolerDrawSpot(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts,
        Vec2 point, float radius, int color) {
        var fracOfCircle = 6;
        for (int i = 0; i < fracOfCircle; i++) {
            // We do need rects, irritatingly
            // so we do fake triangles
            vertexCol(mat, normal, light, verts, color, point.x, point.y);
            vertexCol(mat, normal, light, verts, color, point.x, point.y);
            for (int j = 0; j <= 1; j++) {
                var theta = (i - j) / (float) fracOfCircle * Mth.TWO_PI;
                var rx = Mth.cos(theta) * radius + point.x;
                var ry = Mth.sin(theta) * radius + point.y;
                vertexCol(mat, normal, light, verts, color, rx, ry);
            }
        }
    }
}
