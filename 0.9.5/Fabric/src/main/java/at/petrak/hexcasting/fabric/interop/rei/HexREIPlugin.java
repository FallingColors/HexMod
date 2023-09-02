package at.petrak.hexcasting.fabric.interop.rei;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.interop.utils.PhialRecipeStackBuilder;
import com.google.common.collect.ImmutableSet;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.level.ItemLike;

import java.util.Set;

public class HexREIPlugin implements REIClientPlugin {
	public static final CategoryIdentifier<BrainsweepRecipeDisplay> BRAINSWEEP = CategoryIdentifier.of(BrainsweepRecipeCategory.UID);
	public static final CategoryIdentifier<PhialRecipeDisplay> PHIAL = CategoryIdentifier.of(PhialRecipeCategory.UID);
	public static final CategoryIdentifier<EdifyRecipeDisplay> EDIFY = CategoryIdentifier.of(EdifyRecipeCategory.UID);

	@Override
	public void registerCategories(CategoryRegistry registry) {
		registry.add(new BrainsweepRecipeCategory(), new PhialRecipeCategory(), new EdifyRecipeCategory());
		Set<ItemLike> wands = ImmutableSet.of(
				HexItems.WAND_OAK,
				HexItems.WAND_SPRUCE,
				HexItems.WAND_BIRCH,
				HexItems.WAND_JUNGLE,
				HexItems.WAND_ACACIA,
				HexItems.WAND_DARK_OAK,
				HexItems.WAND_CRIMSON,
				HexItems.WAND_WARPED,
				HexItems.WAND_AKASHIC);
		for (ItemLike wand : wands) {
			registry.addWorkstations(BRAINSWEEP, EntryStacks.of(wand));
			registry.addWorkstations(PHIAL, EntryStacks.of(wand));
			registry.addWorkstations(EDIFY, EntryStacks.of(wand));
		}

		registry.removePlusButton(BRAINSWEEP);
		registry.removePlusButton(PHIAL);
		registry.removePlusButton(EDIFY);
	}

	@Override
	public void registerDisplays(DisplayRegistry helper) {
		helper.registerFiller(BrainsweepRecipe.class, BrainsweepRecipeDisplay::new);
		if (PhialRecipeStackBuilder.shouldAddRecipe()) {
			helper.add(new PhialRecipeDisplay());
		}
		helper.add(new EdifyRecipeDisplay());
	}
}
