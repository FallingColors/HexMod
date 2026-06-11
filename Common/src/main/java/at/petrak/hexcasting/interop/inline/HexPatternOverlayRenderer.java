package at.petrak.hexcasting.interop.inline;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.client.render.PatternColors;
import at.petrak.hexcasting.client.render.PatternRenderer;
import at.petrak.hexcasting.client.render.PatternSettings;
import at.petrak.hexcasting.client.render.RenderLib;
import at.petrak.hexcasting.common.items.storage.ItemScroll;
import at.petrak.hexcasting.common.items.storage.ItemSlate;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import com.samsthenerd.inline.api.client.extrahooks.ItemOverlayRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

@FunctionalInterface
public interface HexPatternOverlayRenderer extends ItemOverlayRenderer {
    HexPattern getPattern(ItemStack stack);

    HexPatternOverlayRenderer SCROLL_RENDERER = (stack) -> {
        var pattern = stack.get(HexDataComponents.PATTERN);
        if (pattern != null && !stack.has(HexDataComponents.NEEDS_PURCHASE)) {
            return pattern;
        }
        return null;
    };

    HexPatternOverlayRenderer SLATE_RENDERER = (stack) -> ItemSlate.getPattern(stack).orElse(null);

    PatternSettings OVERLAY_RENDER_SETTINGS = new PatternSettings("overlay",
        new PatternSettings.PositionSettings(14, 14, 0.5, 0.5,
            PatternSettings.AxisAlignment.CENTER_FIT, PatternSettings.AxisAlignment.CENTER_FIT, 4.0, 0, 0),
        PatternSettings.StrokeSettings.fromStroke(1.5),
        new PatternSettings.ZappySettings(10, 0, 0, 0,
            PatternSettings.ZappySettings.READABLE_OFFSET, 0.7f)
    );

    // higher contrast purple based on slate wobble color
    PatternColors HC_PURPLE_COLOR =
        new PatternColors(RenderLib.screenCol(0xff_cfa0f3), 0xff_763fa1);

    @Override
    default void render(ItemStack itemStack, GuiGraphics guiGraphics) {
        if(!Screen.hasShiftDown()) return;
        HexPattern pat = getPattern(itemStack);
        if(pat == null) return;
        var ps = guiGraphics.pose();
        ps.pushPose();
        ps.translate(1,1,100);
        PatternRenderer.renderPattern(pat, guiGraphics.pose(), OVERLAY_RENDER_SETTINGS, HC_PURPLE_COLOR, 0, 16);
        ps.popPose();
    }
}
