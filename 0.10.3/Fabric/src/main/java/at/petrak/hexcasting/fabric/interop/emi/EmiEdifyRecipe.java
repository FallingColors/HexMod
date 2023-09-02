package at.petrak.hexcasting.fabric.interop.emi;

import at.petrak.hexcasting.common.lib.HexBlocks;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class EmiEdifyRecipe implements EmiRecipe {
    private static final ResourceLocation OVERLAY = modLoc("textures/gui/edify_jei.png");

    private final EmiIngredient saplings;
    private final EmiIngredient leaves;
    private final EmiIngredient log;

    public EmiEdifyRecipe() {
        this.saplings = EmiIngredient.of(ItemTags.SAPLINGS);
        this.leaves = EmiIngredient.of(List.of(
            EmiStack.of(HexBlocks.AMETHYST_EDIFIED_LEAVES),
            EmiStack.of(HexBlocks.AVENTURINE_EDIFIED_LEAVES),
            EmiStack.of(HexBlocks.CITRINE_EDIFIED_LEAVES)
        ));
        this.log = EmiStack.of(HexBlocks.EDIFIED_LOG);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return HexEMIPlugin.EDIFY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return HexEMIPlugin.EDIFY_ID;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(saplings);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return Stream.concat(leaves.getEmiStacks().stream(), log.getEmiStacks().stream()).collect(Collectors.toList());
    }

    @Override
    public int getDisplayWidth() {
        return 79;
    }

    @Override
    public int getDisplayHeight() {
        return 61;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(OVERLAY, 0, 0, getDisplayWidth(), getDisplayHeight(), 0, 0, getDisplayWidth(), getDisplayHeight(), 128, 128);
        widgets.addSlot(saplings, 11, 21).drawBack(false).custom(null, 0, 0, 19, 19);
        widgets.addGeneratedSlot(r -> {
            var stacks = leaves.getEmiStacks();
            return stacks.get(r.nextInt(stacks.size()));
        }, 0, 50, 9).drawBack(false).recipeContext(this).custom(null, 0, 0, 19, 19);
        widgets.addSlot(log, 50, 34).drawBack(false).recipeContext(this).custom(null, 0, 0, 19, 19);
    }
}
