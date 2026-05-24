package at.petrak.hexcasting.interop.patchouli;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import vazkii.patchouli.api.IVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * > no this is a "literally copy these files/parts of file into your mod"
 * > we should put this in patchy but lol
 * > lazy
 * -- Hubry Vazcord
 */
public class PatchouliUtils {
    @SuppressWarnings("unchecked")
    public static <T extends Recipe<I>, I extends RecipeInput> T getRecipe(RecipeType<T> type, ResourceLocation id) {
        // PageDoubleRecipeRegistry
        if (Minecraft.getInstance().level == null) {
            return null;
        } else {
            var manager = Minecraft.getInstance().level.getRecipeManager();

            return (T) manager.byKey(id)
                .map(RecipeHolder::value)
                .filter((recipe) -> recipe.getType() == type).orElse(null);
        }
    }

    /**
     * Combines the ingredients, returning the first matching stack of each, then the second stack of each, etc.
     * looping back ingredients that run out of matched stacks, until the ingredients reach the length
     * of the longest ingredient in the recipe set.
     *
     * @param ingredients           List of ingredients in the specific slot
     * @param longestIngredientSize Longest ingredient in the entire recipe
     * @return Serialized Patchouli ingredient string
     */
    public static IVariable interweaveIngredients(List<Ingredient> ingredients, int longestIngredientSize, HolderLookup.RegistryLookup.Provider registries) {
        if (ingredients.size() == 1) {
            return IVariable.wrapList(Arrays.stream(ingredients.get(0).getItems())
                    .map(v -> IVariable.from(v, registries))
                    .collect(Collectors.toList()), registries);
        }

        ItemStack[] empty = {ItemStack.EMPTY};
        List<ItemStack[]> stacks = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) {
                stacks.add(ingredient.getItems());
            } else {
                stacks.add(empty);
            }
        }
        List<IVariable> list = new ArrayList<>(stacks.size() * longestIngredientSize);
        for (int i = 0; i < longestIngredientSize; i++) {
            for (ItemStack[] stack : stacks) {
                list.add(IVariable.from(stack[i % stack.length], registries));
            }
        }
        return IVariable.wrapList(list, registries);
    }

    /**
     * Overload of the method above that uses the provided list's longest ingredient size.
     */
    public static IVariable interweaveIngredients(List<Ingredient> ingredients, HolderLookup.RegistryLookup.Provider registries) {
        return interweaveIngredients(ingredients,
            ingredients.stream().mapToInt(ingr -> ingr.getItems().length).max().orElse(1), registries
        );
    }
}
