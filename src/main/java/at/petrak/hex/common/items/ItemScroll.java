package at.petrak.hex.common.items;

import at.petrak.hex.api.PatternRegistry;
import at.petrak.hex.hexmath.HexPattern;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class ItemScroll extends Item {
    public static final String TAG_OP_ID = "op_id";

    public ItemScroll(Properties pProperties) {
        super(pProperties);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void makeTooltip(RenderTooltipEvent.GatherComponents evt) {
        ItemStack stack = evt.getItemStack();
        if (!stack.isEmpty() && stack.getItem() instanceof ItemScroll) {
            var tooltip = evt.getTooltipElements();

            var tag = stack.getOrCreateTag();
            ResourceLocation opId = null;
            if (tag.contains(TAG_OP_ID)) {
                opId = ResourceLocation.tryParse(tag.getString(TAG_OP_ID));
            }

            tooltip.add(Either.right(new TooltipGreeble(opId)));
        }
    }

    // https://github.com/VazkiiMods/Quark/blob/master/src/main/java/vazkii/quark/content/client/tooltip/MapTooltips.java
    // yoink
    public static class TooltipGreeble implements ClientTooltipComponent, TooltipComponent {
        private static final ResourceLocation MAP_BG = new ResourceLocation(
                "minecraft:textures/map/map_background.png");

        @Nullable
        private final ResourceLocation opId;
        @Nullable
        private final HexPattern pattern;

        public TooltipGreeble(@Nullable ResourceLocation opId) {
            this.opId = opId;
            if (this.opId != null) {
                this.pattern = PatternRegistry.lookupPerWorldPattern(opId).getFirst();
            } else {
                this.pattern = null;
            }
        }

        @Override
        public void renderText(Font pFont, int pX, int pY, Matrix4f pMatrix4f,
                MultiBufferSource.BufferSource pBufferSource) {

        }

        @Override
        public void renderImage(Font font, int mouseX, int mouseY, PoseStack ps, ItemRenderer pItemRenderer,
                int pBlitOffset) {
            renderBG(ps, mouseX, mouseY);

            // renderText happens *before* renderImage for some asinine reason
            if (this.pattern != null) {
                ps.pushPose();
                ps.translate(mouseX, mouseY, 100);

                var text = this.pattern.toString();
                var tw = font.width(text);
                font.drawShadow(ps, text, this.getWidth(font) / 2f - tw / 2f, 10, -1);

                ps.popPose();
            }
        }

        private static void renderBG(PoseStack ps, int x, int y) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShaderTexture(0, MAP_BG);

            ps.pushPose();
            ps.translate(x, y, -1);
            RenderSystem.enableBlend();

            // i wish i liked mobius front enough ot get to the TIS puzzles
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            Matrix4f neo = ps.last().pose();

            float size = 128;
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            // can i rearrange this?
            buffer.vertex(neo, 0, 0, 0.0F).uv(0.0F, 0.0F).endVertex();
            buffer.vertex(neo, 0, size, 0.0F).uv(0.0F, 1.0f).endVertex();
            buffer.vertex(neo, size, size, 0.0F).uv(1.0F, 1.0f).endVertex();
            buffer.vertex(neo, size, 0, 0.0F).uv(1.0F, 0.0F).endVertex();
            buffer.end();
            BufferUploader.end(buffer);
            ps.popPose();
        }

        @Override
        public int getWidth(Font pFont) {
            return 128;
        }

        @Override
        public int getHeight() {
            return 128;
        }
    }
}
