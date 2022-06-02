package at.petrak.hexcasting.fabric.interop.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiWorldRecipe;
import net.minecraft.resources.ResourceLocation;

public class EmiProfessionRecipe extends EmiWorldRecipe {
	public EmiProfessionRecipe(EmiIngredient input, EmiIngredient catalyst, EmiStack result, ResourceLocation id) {
		super(input, catalyst, result, id);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return HexEMIPlugin.VILLAGER_PROFESSION;
	}
}
