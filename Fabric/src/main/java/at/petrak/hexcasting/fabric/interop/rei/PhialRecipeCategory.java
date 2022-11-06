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

public class PhialRecipeCategory implements DisplayCategory<PhialRecipeDisplay> {
    public static final ResourceLocation UID = modLoc("craft/battery");

    private final ResourceLocation OVERLAY = modLoc("textures/gui/phial.png");

    private final Renderer icon;
    private final Component localizedName;

    public PhialRecipeCategory() {
        localizedName = new TranslatableComponent("hexcasting.spell." + UID);
        icon = new PatternRendererREI(UID, 12, 12).shift(2, 2);
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
    public List<Widget> setupDisplay(PhialRecipeDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));
        widgets.add(Widgets.createDrawableWidget(((helper, matrices, mouseX, mouseY, delta) -> {
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, OVERLAY);
            GuiComponent.blit(matrices, bounds.getMinX(), bounds.getMinY(), 0, 0, getDisplayWidth(display), getDisplayHeight(), 128, 128);
            RenderSystem.disableBlend();
        })));
        widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + 12, bounds.getMinY() + 12)).entries(display.getInputEntries().get(0)).disableBackground());
        widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + 47, bounds.getMinY() + 12)).entries(display.getInputEntries().get(1)).disableBackground());
        widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + 85, bounds.getMinY() + 12)).entries(display.getOutputEntries().get(0)).disableBackground());

        return widgets;
    }

    @Override
    public CategoryIdentifier<? extends PhialRecipeDisplay> getCategoryIdentifier() {
        return HexREIPlugin.PHIAL;
    }

    @Override
    public int getDisplayHeight() {
        return 40;
    }

    @Override
    public int getDisplayWidth(PhialRecipeDisplay display) {
        return 113;
    }
}
