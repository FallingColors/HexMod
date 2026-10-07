package at.petrak.hexcasting.interop.patchouli;

import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.common.items.magic.ItemMediaBattery;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
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
    public static <T extends Recipe<C>, C extends Container> T getRecipe(RecipeType<T> type, ResourceLocation id) {
        // PageDoubleRecipeRegistry
        if (Minecraft.getInstance().level == null) {
            return null;
        } else {
            var manager = Minecraft.getInstance().level.getRecipeManager();
            return (T) manager.byKey(id)
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
    public static IVariable interweaveIngredients(List<Ingredient> ingredients, int longestIngredientSize) {
        if (ingredients.size() == 1) {
            return IVariable.wrapList(Arrays.stream(ingredients.get(0).getItems()).map(IVariable::from).collect(
                Collectors.toList()));
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
                list.add(IVariable.from(stack[i % stack.length]));
            }
        }
        return IVariable.wrapList(list);
    }

    /**
     * Overload of the method above that uses the provided list's longest ingredient size.
     */
    public static IVariable interweaveIngredients(List<Ingredient> ingredients) {
        return interweaveIngredients(ingredients,
            ingredients.stream().mapToInt(ingr -> ingr.getItems().length).max().orElse(1));
    }

    /**
     * Returns a list of ItemStacks representing a media cost.
     * @param mediaCost The amount of media to represent
     * @param fallbackToPhial If true, and the amount of media does not cleanly divide into a multiple of dust, return a phial of that exact value instead. If false, rounds up to the nearest dust.
     */
    public static List<ItemStack> mediaItems(long mediaCost, boolean fallbackToPhial){
        record ItemCost(Item item, int cost) {
            public boolean dividesEvenly (int dividend) {
                return dividend % cost == 0;
            }
        }
        ItemCost[] costs  = {
            new ItemCost(HexItems.AMETHYST_DUST, (int)MediaConstants.DUST_UNIT),
            new ItemCost(Items.AMETHYST_SHARD, (int)MediaConstants.SHARD_UNIT),
            new ItemCost(HexItems.CHARGED_AMETHYST, (int)MediaConstants.CRYSTAL_UNIT),
        };

        // get evenly divisible ItemStacks
        List<ItemStack> validItemStacks = Arrays.stream(costs)
            .filter(itemCost -> itemCost.dividesEvenly((int)mediaCost))
            .map(validItemCost -> new ItemStack(validItemCost.item, (int)mediaCost / validItemCost.cost))
            .toList();

        if(!validItemStacks.isEmpty()) return validItemStacks;

        if(fallbackToPhial){
            return List.of(ItemMediaBattery.withMedia(new ItemStack(HexItems.BATTERY), mediaCost, mediaCost));
        }else{
            // fallback: display in terms of dust, rounded up to the nearest dust
            return List.of(new ItemStack(HexItems.AMETHYST_DUST, (int)Math.ceil((double)mediaCost / MediaConstants.DUST_UNIT)));
        }
    }
}
