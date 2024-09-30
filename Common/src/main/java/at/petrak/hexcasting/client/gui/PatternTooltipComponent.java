package at.petrak.hexcasting.client.gui;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.client.render.PatternColors;
import at.petrak.hexcasting.client.render.PatternRenderer;
import at.petrak.hexcasting.client.render.WorldlyPatternRenderHelpers;
import at.petrak.hexcasting.common.misc.PatternTooltip;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// https://github.com/VazkiiMods/Botania/blob/95bd2d3fbc857b7c102687554e1d1b112f8af436/Xplat/src/main/java/vazkii/botania/client/gui/ManaBarTooltipComponent.java
// yoink

/**
 * @see PatternTooltip the associated data for this
 */
public class PatternTooltipComponent implements ClientTooltipComponent {
    public static final ResourceLocation PRISTINE_BG = modLoc("textures/gui/scroll.png");
    public static final ResourceLocation ANCIENT_BG = modLoc("textures/gui/scroll_ancient.png");
    public static final ResourceLocation SLATE_BG = modLoc("textures/gui/slate.png");

    private static final float RENDER_SIZE = 128f;
    private static final int TEXTURE_SIZE = 48;

    private final HexPattern pattern;
    private final ResourceLocation background;

    public PatternTooltipComponent(PatternTooltip tt) {
        this.pattern = tt.pattern();
        this.background = tt.background();
    }

    @Nullable
    public static ClientTooltipComponent tryConvert(TooltipComponent cmp) {
        if (cmp instanceof PatternTooltip ptt) {
            return new PatternTooltipComponent(ptt);
        }
        return null;
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics graphics) {
        var ps = graphics.pose();

        // far as i can tell "mouseX" and "mouseY" are actually the positions of the corner of the tooltip
        ps.pushPose();
        ps.translate(mouseX, mouseY, 500);
        RenderSystem.enableBlend();
        renderBG(graphics, this.background);

        // renderText happens *before* renderImage for some asinine reason
        ps.translate(0, 0, 100);
        ps.scale(RENDER_SIZE, RENDER_SIZE, 1);

        PatternRenderer.renderPattern(pattern, ps, WorldlyPatternRenderHelpers.READABLE_SCROLL_SETTINGS,
                (PatternRenderer.shouldDoStrokeGradient() ? PatternColors.DEFAULT_GRADIENT_COLOR : PatternColors.DEFAULT_PATTERN_COLOR)
                        .withDots(true, true),
                0, 512);

        ps.popPose();
    }

    private static void renderBG(GuiGraphics graphics, ResourceLocation background) {
        graphics.blit(
            background, // texture
            0, 0, // x, y
            (int) RENDER_SIZE, (int) RENDER_SIZE, // renderWidth, renderHeight
            0f, 0f, // u, v (textureCoords)
            TEXTURE_SIZE, TEXTURE_SIZE, // regionWidth, regionHeight (texture sample dimensions)
            TEXTURE_SIZE, TEXTURE_SIZE); // textureWidth, textureHeight (total dimensions of texture)
    }

    @Override
    public int getWidth(Font pFont) {
        return (int) RENDER_SIZE;
    }

    @Override
    public int getHeight() {
        return (int) RENDER_SIZE;
    }
}
