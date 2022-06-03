package at.petrak.hexcasting.fabric.interop.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.resources.ResourceLocation;

public class EmiProfessionRecipe extends EmiVillagerRecipe {
	public EmiProfessionRecipe(EmiIngredient input, EmiIngredient catalyst, VillagerEmiStack result, ResourceLocation id) {
		super(input, catalyst, result, id);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return HexEMIPlugin.VILLAGER_PROFESSION;
	}
}
