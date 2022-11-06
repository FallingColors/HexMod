package at.petrak.hexcasting.datagen.recipe.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Largely adapted from the following classes:
// https://github.com/Creators-of-Create/Create/blob/82be76d8934af03b4e52cad6a9f74a4175ba7b05/src/main/java/com/simibubi/create/content/contraptions/processing/ProcessingOutput.java
// https://github.com/Creators-of-Create/Create/blob/82be76d8934af03b4e52cad6a9f74a4175ba7b05/src/main/java/com/simibubi/create/foundation/data/recipe/ProcessingRecipeGen.java
// https://github.com/Creators-of-Create/Create/blob/82be76d8934af03b4e52cad6a9f74a4175ba7b05/src/main/java/com/simibubi/create/content/contraptions/processing/ProcessingRecipeBuilder.java
// https://github.com/Creators-of-Create/Create/blob/82be76d8934af03b4e52cad6a9f74a4175ba7b05/src/main/java/com/simibubi/create/content/contraptions/processing/ProcessingRecipeSerializer.java
public class CreateCrushingRecipeBuilder implements RecipeBuilder {
	private String group = "";
	private Ingredient input;
	private final List<ProcessingOutput> results = new ArrayList<>();
	private int processingTime = 100;

	@Override
	public @NotNull CreateCrushingRecipeBuilder unlockedBy(@NotNull String name, @NotNull CriterionTriggerInstance trigger) {
		return this;
	}

	@Override
	public @NotNull CreateCrushingRecipeBuilder group(@Nullable String name) {
		group = name;
		return this;
	}

	public CreateCrushingRecipeBuilder duration(int duration) {
		this.processingTime = duration;
		return this;
	}

	public CreateCrushingRecipeBuilder withInput(ItemLike item) {
		return withInput(Ingredient.of(item));
	}

	public CreateCrushingRecipeBuilder withInput(ItemStack stack) {
		return withInput(Ingredient.of(stack));
	}

	public CreateCrushingRecipeBuilder withInput(Ingredient ingredient) {
		this.input = ingredient;
		return this;
	}

	public CreateCrushingRecipeBuilder withOutput(ItemLike output) {
		return withOutput(1f, output, 1);
	}

	public CreateCrushingRecipeBuilder withOutput(float chance, ItemLike output) {
		return withOutput(chance, output, 1);
	}

	public CreateCrushingRecipeBuilder withOutput(ItemLike output, int count) {
		return withOutput(1f, output, count);
	}

	public CreateCrushingRecipeBuilder withOutput(float chance, ItemLike output, int count) {
		return withOutput(new ItemStack(output, count), chance);
	}

	public CreateCrushingRecipeBuilder withOutput(ItemStack output, float chance) {
		this.results.add(new ItemProcessingOutput(output, chance));
		return this;
	}

	public CreateCrushingRecipeBuilder withOutput(String name) {
		return withOutput(1f, name, 1);
	}

	public CreateCrushingRecipeBuilder withOutput(String name, int count) {
		return withOutput(1f, name, count);
	}

	public CreateCrushingRecipeBuilder withOutput(float chance, String name) {
		return withOutput(chance, name, 1);
	}

	public CreateCrushingRecipeBuilder withOutput(float chance, String name, int count) {
		this.results.add(new CompatProcessingOutput(name, count, chance));
		return this;
	}

	@Override
	public @NotNull Item getResult() {
		return Items.AIR; // Irrelevant, we implement serialization ourselves
	}

	@Override
	public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation resourceLocation) {
		consumer.accept(new CrushingRecipe(resourceLocation));
	}

	public class CrushingRecipe implements FinishedRecipe {

		private final ResourceLocation id;

		public CrushingRecipe(ResourceLocation id) {
			this.id = id;
		}

		@Override
		public void serializeRecipeData(@NotNull JsonObject json) {
			json.addProperty("type", "create:crushing");

			if (!group.isEmpty()) {
				json.addProperty("group", group);
			}

			JsonArray jsonIngredients = new JsonArray();
			JsonArray jsonOutputs = new JsonArray();

			jsonIngredients.add(input.toJson());

			results.forEach(o -> jsonOutputs.add(o.serialize()));

			json.add("ingredients", jsonIngredients);
			json.add("results", jsonOutputs);

			int processingDuration = processingTime;
			if (processingDuration > 0) {
				json.addProperty("processingTime", processingDuration);
			}
		}

		@Override
		public @NotNull ResourceLocation getId() {
			return id;
		}

		@Override
		public @NotNull RecipeSerializer<?> getType() {
			return RecipeSerializer.SHAPELESS_RECIPE; // Irrelevant, we implement serialization ourselves
		}

		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return null;
		}
	}

}
