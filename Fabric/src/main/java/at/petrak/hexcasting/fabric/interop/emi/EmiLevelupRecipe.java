package at.petrak.hexcasting.fabric.interop.emi;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;

public class EmiLevelupRecipe extends EmiVillagerRecipe {
	public EmiLevelupRecipe(EmiIngredient input, VillagerEmiStack result, ResourceLocation id) {
		super(input, EmiStack.of(Items.EMERALD), result, id, false);
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
