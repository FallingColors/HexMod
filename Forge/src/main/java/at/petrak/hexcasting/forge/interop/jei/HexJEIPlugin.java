package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexItems;
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

import static at.petrak.hexcasting.api.HexAPI.modLoc;

@JeiPlugin
public class HexJEIPlugin implements IModPlugin {
    private static final ResourceLocation UID = modLoc(HexAPI.MOD_ID);

    public static final RecipeType<BrainsweepRecipe> BRAINSWEEPING =
        RecipeType.create(HexAPI.MOD_ID, "brainsweeping", BrainsweepRecipe.class);

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
            registration.addRecipes(BRAINSWEEPING,
                level.getRecipeManager().getAllRecipesFor(HexRecipeSerializers.BRAINSWEEP_TYPE));
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(HexItems.STAFF_OAK), BRAINSWEEPING);
        registration.addRecipeCatalyst(new ItemStack(HexItems.STAFF_SPRUCE), BRAINSWEEPING);
        registration.addRecipeCatalyst(new ItemStack(HexItems.STAFF_BIRCH), BRAINSWEEPING);
        registration.addRecipeCatalyst(new ItemStack(HexItems.STAFF_JUNGLE), BRAINSWEEPING);
        registration.addRecipeCatalyst(new ItemStack(HexItems.STAFF_ACACIA), BRAINSWEEPING);
        registration.addRecipeCatalyst(new ItemStack(HexItems.STAFF_DARK_OAK), BRAINSWEEPING);
        registration.addRecipeCatalyst(new ItemStack(HexItems.STAFF_CRIMSON), BRAINSWEEPING);
        registration.addRecipeCatalyst(new ItemStack(HexItems.STAFF_WARPED), BRAINSWEEPING);
        registration.addRecipeCatalyst(new ItemStack(HexItems.STAFF_EDIFIED), BRAINSWEEPING);
    }
}
