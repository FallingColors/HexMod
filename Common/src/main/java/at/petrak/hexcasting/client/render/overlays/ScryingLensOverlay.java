package at.petrak.hexcasting.client.render.overlays;

import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry;
import at.petrak.hexcasting.common.lib.HexAttributes;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class ScryingLensOverlay {
    static void tryRenderScryingLensOverlay(GuiGraphics graphics, float partialTicks) {
        var mc = Minecraft.getInstance();
        var ps = graphics.pose();

        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) {
            return;
        }

        if (player.getAttributeValue(HexAttributes.SCRY_SIGHT) <= 0.0)
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
