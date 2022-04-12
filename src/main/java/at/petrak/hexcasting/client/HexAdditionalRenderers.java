package at.petrak.hexcasting.client;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.api.player.Sentinel;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.api.player.HexPlayerDataHelper;
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
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.BiConsumer;

public class HexAdditionalRenderers {
    @SubscribeEvent
    public static void overlayLevel(RenderLevelLastEvent evt) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            var sentinel = HexPlayerDataHelper.getSentinel(player);
            if (sentinel.hasSentinel() && player.getLevel().dimension().equals(sentinel.dimension())) {
                renderSentinel(sentinel, player, evt.getPoseStack(), evt.getPartialTick());
            }
        }
    }

    @SubscribeEvent
    public static void overlayGui(RenderGameOverlayEvent.Post evt) {
        if (evt.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            tryRenderScryingLensOverlay(evt.getMatrixStack(), evt.getPartialTicks());
        }
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

        var time = mc.level.getLevelData().getGameTime() + partialTicks;
        var bobSpeed = 1f / 20;
        var magnitude = 0.1f;
        ps.translate(0, Mth.sin(bobSpeed * time) * magnitude, 0);
        var spinSpeed = 1f / 30;
        ps.mulPose(Quaternion.fromXYZ(new Vector3f(0, spinSpeed * time, 0)));
        if (sentinel.extendsRange()) {
            ps.mulPose(Quaternion.fromXYZ(new Vector3f(spinSpeed * time / 8f, 0, 0)));
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
        RenderSystem.lineWidth(5f);

        var colorizer = HexPlayerDataHelper.getColorizer(mc.player);
        BiConsumer<float[], float[]> v = (l, r) -> {
            int lcolor = colorizer.getColor(time, new Vec3(l[0], l[1], l[2])),
                    rcolor = colorizer.getColor(time, new Vec3(r[0], r[1], r[2]));
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

    // My internet is really shaky right now but thank god Patchi already does this exact thing
    // cause it's a dependency so i have the .class files downloaded
    private static void tryRenderScryingLensOverlay(PoseStack ps, float partialTicks) {
        var mc = Minecraft.getInstance();

        boolean foundLens = false;
        InteractionHand lensHand = null;
        for (var hand : InteractionHand.values()) {
            if (mc.player.getItemInHand(hand).is(HexItems.SCRYING_LENS.get())) {
                lensHand = hand;
                foundLens = true;
                break;
            }
        }
        if (!foundLens && mc.player.getItemBySlot(EquipmentSlot.HEAD).is(HexItems.SCRYING_LENS.get())) {
            foundLens = true;
        }

        if (!foundLens) {
            return;
        }

        var hitRes = mc.hitResult;
        if (hitRes instanceof BlockHitResult bhr) {
            var pos = bhr.getBlockPos();
            var bs = mc.level.getBlockState(pos);

            var lines = ScryingLensOverlayRegistry.getLines(bs, pos, mc.player, mc.level, lensHand);
            if (lines != null) {
                var window = mc.getWindow();
                var x = window.getGuiScaledWidth() / 2f + 8f;
                var y = window.getGuiScaledHeight() / 2f;
                ps.pushPose();
                ps.translate(x, y, 0);

                var maxWidth = (int) (window.getGuiScaledWidth() / 2f * 0.8f);

                for (var pair : lines) {

                    var stack = pair.getFirst();
                    if (stack != null) {
                        // this draws centered in the Y ...
                        RenderLib.renderItemStackInGui(ps, pair.getFirst(), 0, 0);
                    }
                    float tx = stack == null ? 0 : 18;
                    float ty = 5;
                    // but this draws where y=0 is the baseline
                    var text = pair.getSecond();
                    var textLines = mc.font.getSplitter().splitLines(text, maxWidth, Style.EMPTY);

                    for (var line : textLines) {
                        var actualLine = Language.getInstance().getVisualOrder(line);
                        mc.font.drawShadow(ps, actualLine, tx, ty, 0xffffffff);
                        ps.translate(0, 9, 0);
                    }

                    ps.translate(0, 6, 0);
                }

                ps.popPose();
            }
        }
    }
}
