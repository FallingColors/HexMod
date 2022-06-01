package at.petrak.hexcasting.fabric.interop.rei;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import com.google.common.collect.ImmutableSet;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import java.util.Set;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexREIPlugin implements REIClientPlugin {
	public static final ResourceLocation UID = modLoc("brainsweep");
	public static final CategoryIdentifier<BrainsweepRecipeDisplay> BRAINSWEEP = CategoryIdentifier.of(UID);

	@Override
	public void registerCategories(CategoryRegistry registry) {
		registry.add(new BrainsweepRecipeCategory());
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
		}

		registry.removePlusButton(BRAINSWEEP);
	}

	@Override
	public void registerDisplays(DisplayRegistry helper) {
		helper.registerFiller(BrainsweepRecipe.class, BrainsweepRecipeDisplay::new);
	}
}
