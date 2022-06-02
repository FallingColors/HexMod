package at.petrak.hexcasting.fabric.interop.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiWorldRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;

public class EmiLevelupRecipe extends EmiWorldRecipe {
	private final EmiIngredient input;

	public EmiLevelupRecipe(EmiIngredient input, EmiStack result, ResourceLocation id) {
		super(input, EmiStack.of(Items.EMERALD), result, id, false);
		this.input = input;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return HexEMIPlugin.VILLAGER_LEVELING;
	}
}
