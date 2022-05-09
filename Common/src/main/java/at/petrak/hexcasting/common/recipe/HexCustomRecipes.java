package at.petrak.hexcasting.common.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexCustomRecipes {
    public static void registerSerializers(BiConsumer<RecipeSerializer<?>, ResourceLocation> r) {
        for (var e : RECIPES.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, RecipeSerializer<?>> RECIPES = new LinkedHashMap<>();

    public static final RecipeSerializer<SealFocusRecipe> SEAL_FOCUS = register("seal_focus",
        SealFocusRecipe.SERIALIZER);
    public static final RecipeSerializer<SealSpellbookRecipe> SEAL_SPELLBOOK = register(
        "seal_spellbook",
        SealSpellbookRecipe.SERIALIZER);

    private static <T extends Recipe<?>> RecipeSerializer<T> register(String id, RecipeSerializer<T> recipe) {
        var old = RECIPES.put(modLoc(id), recipe);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return recipe;
    }
}
