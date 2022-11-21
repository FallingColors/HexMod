package at.petrak.hexcasting.client.gui;

import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.client.RenderLib;
import at.petrak.hexcasting.common.misc.PatternTooltip;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

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

    private final HexPattern pattern;
    private final List<Vec2> zappyPoints;
    private final List<Vec2> pathfinderDots;
    private final float scale;
    private final ResourceLocation background;

    public PatternTooltipComponent(PatternTooltip tt) {
        this.pattern = tt.pattern();
        this.background = tt.background();

        var pair = RenderLib.getCenteredPattern(pattern, RENDER_SIZE, RENDER_SIZE, 16f);
        this.scale = pair.getFirst();
        var dots = pair.getSecond();
        this.zappyPoints = RenderLib.makeZappy(
            dots, RenderLib.findDupIndices(pattern.positions()),
            10, 0.8f, 0f, 0f, RenderLib.DEFAULT_READABILITY_OFFSET, RenderLib.DEFAULT_LAST_SEGMENT_LEN_PROP,
            0.0);
        this.pathfinderDots = dots.stream().distinct().collect(Collectors.toList());
    }

    @Nullable
    public static ClientTooltipComponent tryConvert(TooltipComponent cmp) {
        if (cmp instanceof PatternTooltip ptt) {
            return new PatternTooltipComponent(ptt);
        }
        return null;
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, PoseStack ps, ItemRenderer pItemRenderer,
                            int pBlitOffset) {
        var width = this.getWidth(font);
        var height = this.getHeight();

        // far as i can tell "mouseX" and "mouseY" are actually the positions of the corner of the tooltip
        ps.pushPose();
        ps.translate(mouseX, mouseY, 500);
        RenderSystem.enableBlend();
        renderBG(ps, this.background, pBlitOffset);

        // renderText happens *before* renderImage for some asinine reason
//                RenderSystem.disableBlend();
        ps.translate(0, 0, 100);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        ps.translate(width / 2f, height / 2f, 1);

        var mat = ps.last().pose();
        var outer = 0xff_d2c8c8;
        var innerLight = 0xc8_aba2a2;
        var innerDark = 0xc8_322b33;
        RenderLib.drawLineSeq(mat, this.zappyPoints, 6f, 0,
            outer, outer);
        RenderLib.drawLineSeq(mat, this.zappyPoints, 6f * 0.4f, 0,
            innerDark, innerLight);
        RenderLib.drawSpot(mat, this.zappyPoints.get(0), 2.5f, 1f, 0.1f, 0.15f, 0.6f);

        for (var dot : this.pathfinderDots) {
            RenderLib.drawSpot(mat, dot, 1.5f, 0.82f, 0.8f, 0.8f, 0.5f);
        }

        ps.popPose();
    }

    private static void renderBG(PoseStack ps, ResourceLocation background, int blitOffset) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, background);
        // x y blitoffset sw sh w h ... ?
        // parchment doesn't have this mapped
        GuiComponent.blit(ps, 0, 0, blitOffset, 0f, 0f, (int) RENDER_SIZE, (int) RENDER_SIZE, (int) RENDER_SIZE,
            (int) RENDER_SIZE);
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
