package at.petrak.hexcasting.client.entity;

import at.petrak.hexcasting.client.render.RenderLib;
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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

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

            ps.mulPose(Axis.YP.rotationDegrees(180f));
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
                ps.translate(0, 0, 0.01);
                var spotFrac = 0.8f * wallScroll.blockSize;
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

    private static void vertexCol(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts, int col, Vec2 pos) {
        verts.vertex(mat, -pos.x, pos.y, 0)
            .color(col)
            .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
            .normal(normal, 0, 0, 1)
            .endVertex();
    }

    private static void theCoolerDrawLineSeq(Matrix4f mat, Matrix3f normalMat, int light, VertexConsumer verts,
        List<Vec2> points, float width, int color
    ) {
        if (points.size() <= 1) {
            return;
        }

        // TODO: abstract some of this out with RenderLib to stop WET code
        var joinAngles = new float[points.size()];
        var joinOffsets = new float[points.size()];
        for (int i = 2; i < points.size(); i++) {
            var p0 = points.get(i - 2);
            var p1 = points.get(i - 1);
            var p2 = points.get(i);
            var prev = p1.add(p0.negated());
            var next = p2.add(p1.negated());
            var angle = (float) Mth.atan2(
                prev.x * next.y - prev.y * next.x,
                prev.x * next.x + prev.y * next.y);
            joinAngles[i - 1] = angle;
            var clamp = Math.min(prev.length(), next.length()) / (width * 0.5f);
            joinOffsets[i - 1] = Mth.clamp(Mth.sin(angle) / (1 + Mth.cos(angle)), -clamp, clamp);
        }

        for (var i = 0; i < points.size() - 1; i++) {
            var p1 = points.get(i);
            var p2 = points.get(i + 1);

            var tangent = p2.add(p1.negated()).normalized().scale(width * 0.5f);
            var normal = new Vec2(-tangent.y, tangent.x);

            var jlow = joinOffsets[i];
            var jhigh = joinOffsets[i + 1];

            var p1Down = p1.add(tangent.scale(Math.max(0f, jlow))).add(normal);
            var p1Up = p1.add(tangent.scale(Math.max(0f, -jlow))).add(normal.negated());
            var p2Down = p2.add(tangent.scale(Math.max(0f, jhigh)).negated()).add(normal);
            var p2Up = p2.add(tangent.scale(Math.max(0f, -jhigh)).negated()).add(normal.negated());

            // Draw the chamfer hexagon as two trapezoids
            // the points are in different orders to keep clockwise
            vertexCol(mat, normalMat, light, verts, color, p1);
            vertexCol(mat, normalMat, light, verts, color, p2);
            vertexCol(mat, normalMat, light, verts, color, p2Up);
            vertexCol(mat, normalMat, light, verts, color, p1Up);

            vertexCol(mat, normalMat, light, verts, color, p1);
            vertexCol(mat, normalMat, light, verts, color, p1Down);
            vertexCol(mat, normalMat, light, verts, color, p2Down);
            vertexCol(mat, normalMat, light, verts, color, p2);

            if (i > 0) {
                var sangle = joinAngles[i];
                var angle = Math.abs(sangle);
                var rnormal = normal.negated();
                var joinSteps = Mth.ceil(angle * 180 / (RenderLib.CAP_THETA * Mth.PI));
                if (joinSteps < 1) continue;

                if (sangle < 0) {
                    var prevVert = new Vec2(p1.x - rnormal.x, p1.y - rnormal.y);
                    for (var j = 1; j <= joinSteps; j++) {
                        var fan = RenderLib.rotate(rnormal, -sangle * ((float) j / joinSteps));
                        var fanShift = new Vec2(p1.x - fan.x, p1.y - fan.y);

                        vertexCol(mat, normalMat, light, verts, color, p1);
                        vertexCol(mat, normalMat, light, verts, color, p1);
                        vertexCol(mat, normalMat, light, verts, color, fanShift);
                        vertexCol(mat, normalMat, light, verts, color, prevVert);
                        prevVert = fanShift;
                    }
                } else {
                    var startFan = RenderLib.rotate(normal, -sangle);
                    var prevVert = new Vec2(p1.x - startFan.x, p1.y - startFan.y);
                    for (var j = joinSteps - 1; j >= 0; j--) {
                        var fan = RenderLib.rotate(normal, -sangle * ((float) j / joinSteps));
                        var fanShift = new Vec2(p1.x - fan.x, p1.y - fan.y);

                        vertexCol(mat, normalMat, light, verts, color, p1);
                        vertexCol(mat, normalMat, light, verts, color, p1);
                        vertexCol(mat, normalMat, light, verts, color, fanShift);
                        vertexCol(mat, normalMat, light, verts, color, prevVert);
                        prevVert = fanShift;
                    }
                }
            }
        }

        for (var pair : new Vec2[][]{
            {points.get(0), points.get(1)},
            {points.get(points.size() - 1), points.get(points.size() - 2)}
        }) {
            var point = pair[0];
            var prev = pair[1];

            var tangent = point.add(prev.negated()).normalized().scale(0.5f * width);
            var normal = new Vec2(-tangent.y, tangent.x);
            var joinSteps = Mth.ceil(180f / RenderLib.CAP_THETA);
            for (int j = joinSteps; j > 0; j--) {
                var fan0 = RenderLib.rotate(normal, -Mth.PI * ((float) j / joinSteps));
                var fan1 = RenderLib.rotate(normal, -Mth.PI * ((float) (j - 1) / joinSteps));

                vertexCol(mat, normalMat, light, verts, color, point);
                vertexCol(mat, normalMat, light, verts, color, point);
                vertexCol(mat, normalMat, light, verts, color, point.add(fan1));
                vertexCol(mat, normalMat, light, verts, color, point.add(fan0));
            }
        }
    }

    private static void theCoolerDrawSpot(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts,
        Vec2 point, float radius, int color) {
        var fracOfCircle = 6;
        for (int i = 0; i < fracOfCircle; i++) {
            // We do need rects, irritatingly
            // so we do fake triangles
            vertexCol(mat, normal, light, verts, color, point);
            vertexCol(mat, normal, light, verts, color, point);
            for (int j = 0; j <= 1; j++) {
                var theta = (i - j) / (float) fracOfCircle * Mth.TWO_PI;
                var rx = Mth.cos(theta) * radius + point.x;
                var ry = Mth.sin(theta) * radius + point.y;
                vertexCol(mat, normal, light, verts, color, new Vec2(rx, ry));
            }
        }
    }
}
