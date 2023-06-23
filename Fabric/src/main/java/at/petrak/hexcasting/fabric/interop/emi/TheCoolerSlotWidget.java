package at.petrak.hexcasting.fabric.interop.emi;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

public class TheCoolerSlotWidget extends SlotWidget {

    private final float renderScale;

    public TheCoolerSlotWidget(EmiIngredient stack, int x, int y, float renderScale) {
        super(stack, x, y);
        this.renderScale = renderScale;
    }

    private boolean useOffset = true;
    private float xShift = 0;
    private float yShift = 0;

    public TheCoolerSlotWidget useOffset(boolean offset) {
        useOffset = offset;
        return this;
    }

    public TheCoolerSlotWidget customShift(float xShift, float yShift) {
        this.xShift = xShift;
        this.yShift = yShift;
        return this;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta) {
        var poseStack = graphics.pose();
        Bounds bounds = this.getBounds();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int width = bounds.width();
        int height = bounds.height();
        if (this.drawBack) {
            if (this.textureId != null) {
                graphics.blit(this.textureId, bounds.x(), bounds.y(), width, height, (float)this.u, (float)this.v, width, height, 256, 256);
            } else {
                if (this.output) {
                    EmiTexture.LARGE_SLOT.render(graphics, bounds.x(), bounds.y(), delta);
                } else {
                    EmiTexture.SLOT.render(graphics, bounds.x(), bounds.y(), delta);
                }
            }
        }

        int xOff = useOffset ? (width - 16) / 2 : 0;
        int yOff = useOffset ? (height - 16) / 2 : 0;
        poseStack.pushPose();
        poseStack.translate(bounds.x() + xOff + xShift, bounds.y() + yOff + yShift, 0);
        poseStack.scale(renderScale, renderScale, 1);
        this.getStack().render(graphics, 0, 0, delta);
        if (this.catalyst)
            EmiRender.renderCatalystIcon(this.getStack(), graphics, 0, 0);
        poseStack.popPose();
    }
}
