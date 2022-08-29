package at.petrak.hexcasting.fabric.interop.rei;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class EdifyRecipeCategory implements DisplayCategory<EdifyRecipeDisplay> {
    public static final ResourceLocation UID = modLoc("edify");

    private final ResourceLocation OVERLAY = modLoc("textures/gui/edify.png");

    private final Renderer icon;
    private final Component localizedName;

    public EdifyRecipeCategory() {
        localizedName = new TranslatableComponent("hexcasting.spell." + UID);
        icon = new PatternRendererREI(UID, 16, 16).strokeOrder(false);
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }

    @Override
    public @NotNull Component getTitle() {
        return localizedName;
    }

    @Override
    public List<Widget> setupDisplay(EdifyRecipeDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createDrawableWidget(((helper, matrices, mouseX, mouseY, delta) -> {
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, OVERLAY);
            GuiComponent.blit(matrices, bounds.getMinX(), bounds.getMinY(), 0, 0, getDisplayWidth(display), getDisplayHeight(), 128, 128);
            RenderSystem.disableBlend();
        })));
        widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + 12, bounds.getMinY() + 22)).entries(display.getInputEntries().get(0)).disableBackground());
        widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + 51, bounds.getMinY() + 10)).entries(display.getOutputEntries().get(0)).disableBackground());
        widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + 51, bounds.getMinY() + 35)).entries(display.getOutputEntries().get(1)).disableBackground());

        return widgets;
    }

    @Override
    public CategoryIdentifier<? extends EdifyRecipeDisplay> getCategoryIdentifier() {
        return HexREIPlugin.EDIFY;
    }

    @Override
    public int getDisplayHeight() {
        return 61;
    }

    @Override
    public int getDisplayWidth(EdifyRecipeDisplay display) {
        return 79;
    }
}
