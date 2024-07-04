package at.petrak.hexcasting.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Entity/quad based render functions. Should probably be in RenderLib, but I don't want to rewrite to kotlin rn.
 */
public class TheCoolerRenderLib {

    public static final ResourceLocation WHITE = modLoc("textures/entity/white.png");

    private static void vertexCol(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts, int col, Vec2 pos) {
        verts.vertex(mat, -pos.x, pos.y, 0)
                .color(col)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 0, 1)
                .endVertex();
    }

    public static void theCoolerDrawLineSeq(Matrix4f mat, Matrix3f normalMat, int light, VertexConsumer verts,
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

    public static void theCoolerDrawSpot(Matrix4f mat, Matrix3f normal, int light, VertexConsumer verts,
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
