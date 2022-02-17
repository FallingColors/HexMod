package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.HexMod;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexCustomRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, HexMod.MOD_ID);

    public static final RegistryObject<RecipeSerializer<SealFocusRecipe>> SEAL_FOCUS = RECIPES.register("seal_focus",
        () -> SealFocusRecipe.SERIALIZER);
}
