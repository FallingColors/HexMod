package at.petrak.hexcasting.fabric.interop.rei;

import at.petrak.hexcasting.api.mod.HexItemTags;
import at.petrak.hexcasting.interop.utils.PhialRecipeStackBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PhialRecipeDisplay implements Display {
	protected EntryIngredient inputs;
	protected EntryIngredient bottle;
	protected EntryIngredient outputs;

	public PhialRecipeDisplay() {
		var stacks = PhialRecipeStackBuilder.createStacks();
		this.inputs = EntryIngredients.ofItemStacks(stacks.getFirst());
		this.bottle = EntryIngredients.ofIngredient(Ingredient.of(HexItemTags.PHIAL_BASE));
		this.outputs = EntryIngredients.ofItemStacks(stacks.getSecond());

	}

	@Override
	public @Nonnull List<EntryIngredient> getInputEntries() {
		return List.of(this.inputs, this.bottle);
	}

	@Override
	public @Nonnull List<EntryIngredient> getOutputEntries() {
		return Collections.singletonList(this.outputs);
	}

	@Override
	public @Nonnull Optional<ResourceLocation> getDisplayLocation() {
		return Optional.of(PhialRecipeCategory.UID);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return HexREIPlugin.PHIAL;
	}
}
