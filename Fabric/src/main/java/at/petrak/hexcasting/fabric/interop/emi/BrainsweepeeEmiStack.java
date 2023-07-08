package at.petrak.hexcasting.fabric.interop.emi;

import at.petrak.hexcasting.client.ClientTickCounter;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

import static at.petrak.hexcasting.api.HexAPI.modLoc;
import static at.petrak.hexcasting.client.render.RenderLib.renderEntity;

public class BrainsweepeeEmiStack extends EmiStack {
    public final BrainsweepeeIngredient ingredient;
    private final ResourceLocation id;

    public BrainsweepeeEmiStack(BrainsweepeeIngredient ingr) {
        this.ingredient = ingr;

        var bareId = this.ingredient.getSomeKindOfReasonableIDForEmi();
        this.id = modLoc(bareId);
    }

    @Override
    public EmiStack copy() {
        return new BrainsweepeeEmiStack(this.ingredient);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public CompoundTag getNbt() {
        return null;
    }

    @Override
    public Object getKey() {
        return id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<Component> getTooltipText() {
        Minecraft mc = Minecraft.getInstance();
        boolean advanced = mc.options.advancedItemTooltips;

        return ingredient.getTooltip(advanced);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return getTooltipText().stream()
            .map(Component::getVisualOrderText)
            .map(ClientTooltipComponent::create)
            .collect(Collectors.toList());
    }

    @Override
    public Component getName() {
        return ingredient.getName();
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
        if ((flags & RENDER_ICON) != 0) {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level != null) {
                var example = this.ingredient.exampleEntity(level);

                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                renderEntity(graphics, example, level, x + 8, y + 16, ClientTickCounter.getTotal(), 8, 0, it -> it);
            }
        }

//		if ((flags & RENDER_REMAINDER) != 0) {
//			EmiRender.renderRemainderIcon(this, poseStack, x, y);
//		}
    }
}
