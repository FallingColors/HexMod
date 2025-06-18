package at.petrak.hexcasting.client.render;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.api.utils.QuaternionfUtils;
import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.lib.HexAttributes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiConsumer;

public class HexAdditionalRenderers {
    public static void overlayLevel(PoseStack ps, float partialTick) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            var sentinel = IXplatAbstractions.INSTANCE.getSentinel(player);
            if (sentinel != null && player.level().dimension().equals(sentinel.dimension())) {
                renderSentinel(sentinel, player, ps, partialTick);
            }
        }
    }

    public static void overlayGui(GuiGraphics graphics, float partialTicks) {
        tryRenderScryingLensOverlay(graphics, partialTicks);
    }

    private static void renderSentinel(Sentinel sentinel, LocalPlayer owner,
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
        var neo = ps.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.lineWidth(5f);

        var pigment = IXplatAbstractions.INSTANCE.getPigment(owner);
        var colProvider = pigment.getColorProvider();

        // Icosahedron inscribed inside the unit sphere
        var buf = tess.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        BiConsumer<float[], float[]> v = (l, r) -> {
            int lcolor = colProvider.getColor(time, new Vec3(l[0], l[1], l[2])),
                rcolor = colProvider.getColor(time, new Vec3(r[0], r[1], r[2]));
            var normal = new Vector3f(r[0] - l[0], r[1] - l[1], r[2] - l[2]);
            normal.normalize();
            buf.addVertex(neo, l[0], l[1], l[2])
                .setColor(lcolor)
                .setNormal(ps.last(), normal.x(), normal.y(), normal.z());
            buf.addVertex(neo, r[0], r[1], r[2])
                .setColor(rcolor)
                .setNormal(ps.last(), -normal.x(), -normal.y(), -normal.z());
        };

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
        //tess.end();

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

    private static void tryRenderScryingLensOverlay(GuiGraphics graphics, float partialTicks) {
        var mc = Minecraft.getInstance();
        var ps = graphics.pose();

        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) {
            return;
        }

        if (player.getAttributeValue(HexAttributes.SCRY_SIGHT) <= 0.0 || player.getAttributeValue(HexAttributes.FEEBLE_MIND) > 0)
            return;

        var hitRes = mc.hitResult;
        if (hitRes != null && hitRes.getType() == HitResult.Type.BLOCK) {
            var bhr = (BlockHitResult) hitRes;
            var pos = bhr.getBlockPos();
            var bs = level.getBlockState(pos);

            var lines = ScryingLensOverlayRegistry.getLines(bs, pos, player, level, bhr.getDirection());

            int totalHeight = 8;
            List<Pair<ItemStack, List<FormattedText>>> actualLines = Lists.newArrayList();

            var window = mc.getWindow();
            var maxWidth = (int) (window.getGuiScaledWidth() / 2f * 0.8f);

            for (var pair : lines) {
                totalHeight += mc.font.lineHeight + 6;
                var text = pair.getSecond();
                var textLines = mc.font.getSplitter().splitLines(text, maxWidth, Style.EMPTY);

                actualLines.add(Pair.of(pair.getFirst(), textLines));

                if (textLines.size() > 1) {
                    totalHeight += mc.font.lineHeight * (textLines.size() - 1);
                }
            }

            if (!lines.isEmpty()) {
                var x = window.getGuiScaledWidth() / 2f + 8f;
                var y = window.getGuiScaledHeight() / 2f - totalHeight;
                ps.pushPose();
                ps.translate(x, y, 0);

                for (var pair : actualLines) {
                    var stack = pair.getFirst();
                    if (!stack.isEmpty()) {
                        // this draws centered in the Y ...
                        graphics.renderItem(pair.getFirst(), 0, 0);
                    }
                    int tx = stack.isEmpty() ? 0 : 18;
                    int ty = 5;
                    // but this draws where y=0 is the baseline
                    var text = pair.getSecond();

                    for (var line : text) {
                        var actualLine = Language.getInstance().getVisualOrder(line);
                        graphics.drawString(mc.font, actualLine, tx, ty, 0xffffffff);
                        ps.translate(0, mc.font.lineHeight, 0);
                    }
                    if (text.isEmpty()) {
                        ps.translate(0, mc.font.lineHeight, 0);
                    }
                    ps.translate(0, 6, 0);
                }

                ps.popPose();
            }
        }
    }
}
