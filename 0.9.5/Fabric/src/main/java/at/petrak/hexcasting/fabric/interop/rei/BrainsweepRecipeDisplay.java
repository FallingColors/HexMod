package at.petrak.hexcasting.fabric.interop.rei;

import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BrainsweepRecipeDisplay implements Display {
	protected final BrainsweepRecipe recipe;
	protected EntryIngredient inputs;
	protected EntryIngredient outputs;

	public BrainsweepRecipeDisplay(BrainsweepRecipe recipe) {
		this.recipe = recipe;
		this.inputs = EntryIngredients.ofItemStacks(recipe.blockIn().getDisplayedStacks());
		this.outputs = EntryIngredients.of(recipe.result().getBlock());
	}

	@Override
	public @Nonnull List<EntryIngredient> getInputEntries() {
		return Collections.singletonList(this.inputs);
	}

	@Override
	public @Nonnull List<EntryIngredient> getOutputEntries() {
		return Collections.singletonList(this.outputs);
	}

	@Override
	public @Nonnull Optional<ResourceLocation> getDisplayLocation() {
		return Optional.ofNullable(this.recipe).map(BrainsweepRecipe::getId);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return HexREIPlugin.BRAINSWEEP;
	}
}
