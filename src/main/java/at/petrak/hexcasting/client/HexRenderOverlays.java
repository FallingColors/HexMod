package at.petrak.hexcasting.client;

import at.petrak.hexcasting.common.casting.operators.spells.sentinel.CapSentinel;
import at.petrak.hexcasting.common.lib.LibCapabilities;
import at.petrak.hexcasting.common.lib.RegisterHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HexRenderOverlays {
    @SubscribeEvent
    public static void overlay(RenderGameOverlayEvent evt) {
        var player = Minecraft.getInstance().player;
        var maybeSentinelCap = player.getCapability(LibCapabilities.SENTINEL).resolve();
        if (maybeSentinelCap.isPresent()) {
            var cap = maybeSentinelCap.get();
            renderSentinel(cap, player, evt.getMatrixStack());
        }


    }

    private static void renderSentinel(CapSentinel sentinel, LocalPlayer owner,
        PoseStack ps) {
        ps.pushPose();

        // i am just reading VCC code i have no idea
        var mc = Minecraft.getInstance();
        ps.translate(sentinel.position.x, sentinel.position.y, sentinel.position.z);
        var campos = mc.gameRenderer.getMainCamera().getLookVector();
        ps.translate(-campos.x(), -campos.y(), -campos.z());

        ps.mulPose(mc.gameRenderer.getMainCamera().rotation());
        float scale = 0.0166666f * 1.6f;
        ps.scale(-scale, -scale, scale);

        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        RenderSystem.setShaderTexture(0, RegisterHelper.prefix("entity/sentinel.png"));

        var tess = Tesselator.getInstance();
        var buf = tess.getBuilder();

        var neo = ps.last().pose();
        RenderSystem.disableDepthTest();
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR_TEX);
        buf.vertex(neo, 0f, 0f, 0f).color(sentinel.color).uv(0f, 0f).endVertex();
        buf.vertex(neo, 0f, 1f, 0f).color(sentinel.color).uv(0f, 1f).endVertex();
        buf.vertex(neo, 1f, 0f, 0f).color(sentinel.color).uv(1f, 0f).endVertex();
        buf.vertex(neo, 1f, 1f, 0f).color(sentinel.color).uv(1f, 1f).endVertex();
        tess.end();

        RenderSystem.enableDepthTest();

        ps.popPose();
    }
}
