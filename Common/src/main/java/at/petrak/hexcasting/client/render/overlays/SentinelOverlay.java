package at.petrak.hexcasting.client.render.overlays;

import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.api.utils.QuaternionfUtils;
import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.function.BiConsumer;

public class SentinelOverlay {
    public static void renderSentinel(Sentinel sentinel, LocalPlayer owner,
        PoseStack ps, float partialTicks) {
        ps.pushPose();

        // zero vector is the player
        var mc = Minecraft.getInstance();
        var camera = mc.gameRenderer.getMainCamera();
        var playerPos = camera.getPosition();
        ps.translate(
            sentinel.position().x - playerPos.x,
            sentinel.position().y - playerPos.y,
            sentinel.position().z - playerPos.z);

        var time = ClientTickCounter.getTotal() / 2;
        var bobSpeed = 1f / 20;
        var magnitude = 0.1f;
        ps.translate(0, Mth.sin(bobSpeed * time) * magnitude, 0);
        var spinSpeed = 1f / 30;
        ps.mulPose(QuaternionfUtils.fromXYZ(new Vector3f(0, spinSpeed * time, 0)));
        if (sentinel.extendsRange()) {
            ps.mulPose(QuaternionfUtils.fromXYZ(new Vector3f(spinSpeed * time / 8f, 0, 0)));
        }

        float scale = 0.5f;
        ps.scale(scale, scale, scale);


        var tess = Tesselator.getInstance();
        var buf = tess.getBuilder();
        var neo = ps.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.lineWidth(5f);

        var pigment = IXplatAbstractions.INSTANCE.getPigment(owner);
        var colProvider = pigment.getColorProvider();
        BiConsumer<float[], float[]> v = (l, r) -> {
            int lcolor = colProvider.getColor(time, new Vec3(l[0], l[1], l[2])),
                rcolor = colProvider.getColor(time, new Vec3(r[0], r[1], r[2]));
            var normal = new Vector3f(r[0] - l[0], r[1] - l[1], r[2] - l[2]);
            normal.normalize();
            buf.vertex(neo, l[0], l[1], l[2])
                .color(lcolor)
                .normal(ps.last().normal(), normal.x(), normal.y(), normal.z())
                .endVertex();
            buf.vertex(neo, r[0], r[1], r[2])
                .color(rcolor)
                .normal(ps.last().normal(), -normal.x(), -normal.y(), -normal.z())
                .endVertex();
        };

        // Icosahedron inscribed inside the unit sphere
        buf.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (int side = 0; side <= 1; side++) {
            var ring = (side == 0) ? Icos.BOTTOM_RING : Icos.TOP_RING;
            var apex = (side == 0) ? Icos.BOTTOM : Icos.TOP;

            // top & bottom spider
            for (int i = 0; i < 5; i++) {
                v.accept(apex, ring[i]);
            }

            // ring around
            for (int i = 0; i < 5; i++) {
                v.accept(ring[i % 5], ring[(i + 1) % 5]);
            }
        }
        // center band
        for (int i = 0; i < 5; i++) {
            var bottom = Icos.BOTTOM_RING[i];
            v.accept(Icos.TOP_RING[(i + 2) % 5], bottom);
            v.accept(bottom, Icos.TOP_RING[(i + 3) % 5]);
        }
        tess.end();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        ps.popPose();
    }

    private static class Icos {
        public static float[] TOP = {0, 1, 0};
        public static float[] BOTTOM = {0, -1, 0};
        public static float[][] TOP_RING = new float[5][];
        public static float[][] BOTTOM_RING = new float[5][];

        static {
            var theta = (float) Mth.atan2(0.5, 1);
            for (int i = 0; i < 5; i++) {
                var phi = (float) i / 5f * Mth.TWO_PI;
                var x = Mth.cos(theta) * Mth.cos(phi);
                var y = Mth.sin(theta);
                var z = Mth.cos(theta) * Mth.sin(phi);
                TOP_RING[i] = new float[]{x, y, z};
                BOTTOM_RING[i] = new float[]{-x, -y, -z};
            }
        }
    }
}
