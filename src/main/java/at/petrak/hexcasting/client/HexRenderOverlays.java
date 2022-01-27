package at.petrak.hexcasting.client;

import at.petrak.hexcasting.common.casting.operators.spells.sentinel.CapSentinel;
import at.petrak.hexcasting.common.lib.LibCapabilities;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Consumer;

public class HexRenderOverlays {
    @SubscribeEvent
    public static void overlay(RenderLevelLastEvent evt) {
        var player = Minecraft.getInstance().player;
        var maybeSentinelCap = player.getCapability(LibCapabilities.SENTINEL).resolve();
        if (maybeSentinelCap.isPresent()) {
            var cap = maybeSentinelCap.get();
            if (cap.hasSentinel) {
                renderSentinel(cap, player, evt.getPoseStack(), evt.getPartialTick());
            }
        }
    }

    private static void renderSentinel(CapSentinel sentinel, LocalPlayer owner,
        PoseStack ps, float partialTicks) {
        ps.pushPose();

        // zero vector is the player
        var mc = Minecraft.getInstance();
        var playerPos = mc.gameRenderer.getMainCamera().getPosition();
        ps.translate(
            sentinel.position.x - playerPos.x + 0.5,
            sentinel.position.y - playerPos.y + 0.5,
            sentinel.position.z - playerPos.z + 0.5);

        var time = mc.level.getLevelData().getGameTime() + partialTicks;
        var bobSpeed = 1f / 20;
        var magnitude = 0.1f;
        ps.translate(0, Mth.sin(bobSpeed * time) * magnitude, 0);
        var spinSpeed = 1f / 30;
        ps.mulPose(Quaternion.fromXYZ(new Vector3f(0, spinSpeed * time, 0)));

        float scale = 0.5f;
        ps.scale(scale, scale, scale);


        var tess = Tesselator.getInstance();
        var buf = tess.getBuilder();
        var neo = ps.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(5f);

        // we have to put *something* in the normal lest flickering
        Consumer<float[]> v = (point) -> buf.vertex(neo, point[0], point[1], point[2])
            .color(sentinel.color)
            .normal(point[0], point[1], point[2])
            .endVertex();

        // Icosahedron inscribed inside the unit sphere
        for (int side = 0; side <= 1; side++) {
            var ring = (side == 0) ? Icos.BOTTOM_RING : Icos.TOP_RING;
            var apex = (side == 0) ? Icos.BOTTOM : Icos.TOP;

            // top & bottom spider
            buf.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            for (int i = 0; i < 5; i++) {
                var end = ring[i];
                v.accept(apex);
                v.accept(end);
            }
            tess.end();

            // ring around
            buf.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            for (int i = 0; i <= 5; i++) {
                var point = ring[i % 5];
                v.accept(point);
            }
            tess.end();
        }
        // center band
        buf.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (int i = 0; i < 5; i++) {
            var bottom = Icos.BOTTOM_RING[i];
            var top = Icos.TOP_RING[(i + 3) % 5];
            v.accept(bottom);
            v.accept(top);
        }
        v.accept(Icos.BOTTOM_RING[0]);
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
