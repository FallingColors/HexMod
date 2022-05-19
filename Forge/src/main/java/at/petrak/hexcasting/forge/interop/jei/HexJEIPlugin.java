package at.petrak.hexcasting.interop.jei;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static at.petrak.hexcasting.common.lib.RegisterHelper.prefix;

@JeiPlugin
public class HexJEIPlugin implements IModPlugin {
	private static final ResourceLocation UID = prefix(HexMod.MOD_ID);

	public static final RecipeType<BrainsweepRecipe> BRAINSWEEPING =
		RecipeType.create(HexMod.MOD_ID, "brainsweeping", BrainsweepRecipe.class);

	@NotNull
	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}


	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new BrainsweepRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerRecipes(@NotNull IRecipeRegistration registration) {
		Level level = Minecraft.getInstance().level;
		if (level != null) {
			registration.addRecipes(BRAINSWEEPING, level.getRecipeManager().getAllRecipesFor(HexRecipeSerializers.BRAINSWEEP_TYPE));
		}
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_OAK::get), BRAINSWEEPING);
		registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_SPRUCE::get), BRAINSWEEPING);
		registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_BIRCH::get), BRAINSWEEPING);
		registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_JUNGLE::get), BRAINSWEEPING);
		registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_ACACIA::get), BRAINSWEEPING);
		registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_DARK_OAK::get), BRAINSWEEPING);
		registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_CRIMSON::get), BRAINSWEEPING);
		registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_WARPED::get), BRAINSWEEPING);
		registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_AKASHIC::get), BRAINSWEEPING);
	}
}
