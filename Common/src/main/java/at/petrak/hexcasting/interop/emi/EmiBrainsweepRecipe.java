package at.petrak.hexcasting.interop.emi;

import at.petrak.hexcasting.api.client.ClientEnlightenment;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget.Alignment;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public record EmiBrainsweepRecipe(EmiIngredient blockInput,
                                  EmiIngredient villagerInput,
                                  EmiIngredient mediaCost,
                                  EmiStack output,
                                  ResourceLocation id) implements EmiRecipe {
    private static final ResourceLocation OVERLAY = modLoc("textures/gui/brainsweep_recipe.png");
    private static final Component RECIPE_HIDDEN = Component.translatable("hexcasting.tooltip.brainsweep.recipe_hidden");

    @Override
    public EmiRecipeCategory getCategory() {
        return HexEMIPlugin.BRAINSWEEP;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(blockInput, villagerInput);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth() {
        return ClientEnlightenment.isEnlightened() ? 118 : (Minecraft.getInstance().font.width(RECIPE_HIDDEN) + 8);
    }

    @Override
    public int getDisplayHeight() {
        return ClientEnlightenment.isEnlightened() ? 85 : 48;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        if (ClientEnlightenment.isEnlightened()) {
            widgets.addTexture(OVERLAY, 0, 0, getDisplayWidth(), getDisplayHeight(), 0, 0, getDisplayWidth(), getDisplayHeight(), 128, 128);
            widgets.addSlot(blockInput, 11, 34).drawBack(false).customBackground(null, 0, 0, 19, 19);
            widgets.addSlot(mediaCost, 11, 54).drawBack(false).customBackground(null, 0, 0, 19, 19);

            widgets.add(new EntitySlotWidget(villagerInput, 37, 19, 2.75f).useOffset(false).customShift(-8.5f, 2.485f))
                    .drawBack(false).customBackground(null, 0, 0, 27, 49);

            widgets.addSlot(output, 86, 34).drawBack(false).large(true).recipeContext(this).customBackground(null, 0, 0, 19, 19);
        } else {
            widgets.addText(RECIPE_HIDDEN, getDisplayWidth() / 2, getDisplayHeight() / 2, 0x3f3f3f, false)
                    .horizontalAlign(Alignment.CENTER).verticalAlign(Alignment.CENTER);
        }
    }
}
