package at.petrak.hexcasting.datagen.recipe.builders;

import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry;
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredient;
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredient;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BrainsweepRecipeBuilder implements RecipeBuilder {
	private final StateIngredient blockIn;
	private final BrainsweepeeIngredient entityIn;
	private final long mediaCost;
	private final BlockState result;

	private final Advancement.Builder advancement;

	public BrainsweepRecipeBuilder(StateIngredient blockIn, BrainsweepeeIngredient entityIn, BlockState result,
		long mediaCost) {
		this.blockIn = blockIn;
		this.entityIn = entityIn;
		this.result = result;
		this.mediaCost = mediaCost;
		this.advancement = Advancement.Builder.advancement();
	}

	@Override
	public RecipeBuilder unlockedBy(String pCriterionName, CriterionTriggerInstance pCriterionTrigger) {
		this.advancement.addCriterion(pCriterionName, pCriterionTrigger);
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
	public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
		if (this.advancement.getCriteria().isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + pRecipeId);
		}

		this.advancement.parent(new ResourceLocation("recipes/root"))
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId))
			.rewards(AdvancementRewards.Builder.recipe(pRecipeId))
			.requirements(RequirementsStrategy.OR);
		pFinishedRecipeConsumer.accept(new Result(
			pRecipeId,
			this.blockIn, this.entityIn, this.mediaCost, this.result,
			this.advancement,
			new ResourceLocation(pRecipeId.getNamespace(), "recipes/brainsweep/" + pRecipeId.getPath())));
	}

	public record Result(ResourceLocation id, StateIngredient blockIn, BrainsweepeeIngredient villagerIn,
						 long mediaCost, BlockState result, Advancement.Builder advancement,
						 ResourceLocation advancementId) implements FinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("blockIn", this.blockIn.serialize());
			json.add("entityIn", this.villagerIn.serialize());
			json.addProperty("cost", this.mediaCost);
			json.add("result", StateIngredientHelper.serializeBlockState(this.result));
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return HexRecipeStuffRegistry.BRAINSWEEP;
		}

		@Nullable
		@Override
		public JsonObject serializeAdvancement() {
			return this.advancement.serializeToJson();
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementId() {
			return this.advancementId;
		}
	}
}
