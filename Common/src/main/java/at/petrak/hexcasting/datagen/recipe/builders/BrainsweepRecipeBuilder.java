package at.petrak.hexcasting.datagen.recipe.builders;

import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.ingredient.state.StateIngredient;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BrainsweepRecipeBuilder implements RecipeBuilder {
	private final StateIngredient blockIn;
	private final BrainsweepeeIngredient entityIn;
	private final long mediaCost;
	private final BlockState result;

	private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

	public BrainsweepRecipeBuilder(StateIngredient blockIn, BrainsweepeeIngredient entityIn, BlockState result,
		long mediaCost) {
		this.blockIn = blockIn;
		this.entityIn = entityIn;
		this.result = result;
		this.mediaCost = mediaCost;
	}

	@Override
	public RecipeBuilder unlockedBy(String pCriterionName, Criterion<?> pCriterionTrigger) {
		criteria.put(pCriterionName, pCriterionTrigger);
		return this;
	}

	@Override
	public RecipeBuilder group(@Nullable String pGroupName) {
		return this;
	}

	@Override
	public Item getResult() {
		return this.result.getBlock().asItem();
	}

	@Override
	public void save(RecipeOutput recipeOutput, ResourceLocation id) {
		Advancement.Builder advancement = recipeOutput.advancement()
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
				.rewards(AdvancementRewards.Builder.recipe(id))
				.requirements(AdvancementRequirements.Strategy.OR);
		this.criteria.forEach(advancement::addCriterion);

		var recipe = new BrainsweepRecipe(blockIn, entityIn, mediaCost, result);
		recipeOutput.accept(id.withPrefix("brainsweep/"), recipe, advancement.build(id.withPrefix("recipes/brainsweep/")));
	}
}
