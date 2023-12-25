package at.petrak.hexcasting.fabric.interop.emi;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.interop.utils.PhialRecipeStackBuilder;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class EmiPhialRecipe implements EmiRecipe {
    private static final ResourceLocation OVERLAY = modLoc("textures/gui/phial_jei.png");

    private final EmiIngredient inputs;
    private final EmiIngredient bottle;
    private final EmiIngredient outputs;

    private final int uniq = EmiUtil.RANDOM.nextInt();

    public EmiPhialRecipe() {
        var stacks = PhialRecipeStackBuilder.createStacks();
        this.inputs = EmiIngredient.of(stacks.getFirst().stream().map(EmiStack::of).collect(Collectors.toList()));
        this.bottle = EmiIngredient.of(HexTags.Items.PHIAL_BASE);
        this.outputs = EmiIngredient.of(stacks.getSecond().stream().map(EmiStack::of).collect(Collectors.toList()));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return HexEMIPlugin.PHIAL;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return HexEMIPlugin.PHIAL_ID;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(inputs, bottle);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs.getEmiStacks();
    }

    @Override
    public int getDisplayWidth() {
        return 113;
    }

    @Override
    public int getDisplayHeight() {
        return 40;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(OVERLAY, 0, 0, getDisplayWidth(), getDisplayHeight(), 0, 0, getDisplayWidth(),
            getDisplayHeight(), 128, 128);
        widgets.addGeneratedSlot((r) -> {
            var stacks = inputs.getEmiStacks();
            return stacks.get(r.nextInt(stacks.size()));
        }, uniq, 11, 11).drawBack(false).customBackground(null, 0, 0, 19, 19);
        widgets.addSlot(bottle, 46, 11).drawBack(false).customBackground(null, 0, 0, 19, 19);
        widgets.addGeneratedSlot((r) -> {
            var stacks = outputs.getEmiStacks();
            return stacks.get(r.nextInt(stacks.size()));
        }, uniq, 84, 11).drawBack(false).recipeContext(this).customBackground(null, 0, 0, 19, 19);
    }
}
