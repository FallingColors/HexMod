package at.petrak.hexcasting.fabric.interop.rei;

import at.petrak.hexcasting.common.lib.HexBlocks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class EdifyRecipeDisplay implements Display {
	protected EntryIngredient saplings;
	protected EntryIngredient leaves;
	protected EntryIngredient log;

	public EdifyRecipeDisplay() {

		this.saplings = EntryIngredients.ofIngredient(Ingredient.of(ItemTags.SAPLINGS));
		this.leaves = EntryIngredients.ofItems(List.of(
			HexBlocks.AKASHIC_LEAVES1,
			HexBlocks.AKASHIC_LEAVES2,
			HexBlocks.AKASHIC_LEAVES3
		));
		this.log = EntryIngredients.of(HexBlocks.AKASHIC_LOG);

	}

	@Override
	public @Nonnull List<EntryIngredient> getInputEntries() {
		return List.of(this.saplings);
	}

	@Override
	public @Nonnull List<EntryIngredient> getOutputEntries() {
		return List.of(this.leaves, this.log);
	}

	@Override
	public @Nonnull Optional<ResourceLocation> getDisplayLocation() {
		return Optional.of(EdifyRecipeCategory.UID);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return HexREIPlugin.EDIFY;
	}
}
