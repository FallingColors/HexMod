package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.casting.operators.spells.OpEdifySapling;
import at.petrak.hexcasting.common.casting.operators.spells.OpMakeBattery;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe;
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers;
import at.petrak.hexcasting.interop.utils.PhialRecipeStackBuilder;
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

import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

@JeiPlugin
public class HexJEIPlugin implements IModPlugin {
    private static final ResourceLocation UID = modLoc(HexAPI.MOD_ID);

    public static final RecipeType<BrainsweepRecipe> BRAINSWEEPING =
        RecipeType.create(HexAPI.MOD_ID, "brainsweeping", BrainsweepRecipe.class);

    // Only one entry, might as well use the op class
    public static final RecipeType<OpMakeBattery> PHIAL =
        RecipeType.create(HexAPI.MOD_ID, "craft_phial", OpMakeBattery.class);
    public static final RecipeType<OpEdifySapling> EDIFY =
        RecipeType.create(HexAPI.MOD_ID, "edify_tree", OpEdifySapling.class);

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
            new BrainsweepRecipeCategory(guiHelper),
            new PhialRecipeCategory(guiHelper),
            new EdifyRecipeCategory(guiHelper));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            registration.addRecipes(BRAINSWEEPING,
                level.getRecipeManager().getAllRecipesFor(HexRecipeSerializers.BRAINSWEEP_TYPE));
        }

        if (PhialRecipeStackBuilder.shouldAddRecipe()) {
            registration.addRecipes(PHIAL, List.of(OpMakeBattery.INSTANCE));
        }

        registration.addRecipes(EDIFY, List.of(OpEdifySapling.INSTANCE));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_OAK), BRAINSWEEPING, PHIAL, EDIFY);
        registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_SPRUCE), BRAINSWEEPING, PHIAL, EDIFY);
        registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_BIRCH), BRAINSWEEPING, PHIAL, EDIFY);
        registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_JUNGLE), BRAINSWEEPING, PHIAL, EDIFY);
        registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_ACACIA), BRAINSWEEPING, PHIAL, EDIFY);
        registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_DARK_OAK), BRAINSWEEPING, PHIAL, EDIFY);
        registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_CRIMSON), BRAINSWEEPING, PHIAL, EDIFY);
        registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_WARPED), BRAINSWEEPING, PHIAL, EDIFY);
        registration.addRecipeCatalyst(new ItemStack(HexItems.WAND_AKASHIC), BRAINSWEEPING, PHIAL, EDIFY);
    }
}
