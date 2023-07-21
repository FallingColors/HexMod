package at.petrak.hexcasting.client.render.overlays;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class HexAdditionalRenderers {
    public static void overlayLevel(PoseStack ps, float partialTick) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            var sentinel = IXplatAbstractions.INSTANCE.getSentinel(player);
            if (sentinel != null && player.level().dimension().equals(sentinel.dimension())) {
                SentinelOverlay.renderSentinel(sentinel, player, ps, partialTick);
            }
        }
    }

    public static void overlayGui(GuiGraphics graphics, float partialTicks) {
        ScryingLensOverlay.tryRenderScryingLensOverlay(graphics, partialTicks);
        EigengrauOverlay.renderEigengrau();
    }
}
