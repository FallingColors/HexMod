package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.common.items.HexItems;
import at.petrak.hexcasting.common.items.ItemFocus;
import at.petrak.hexcasting.common.recipe.ingredient.UnsealedIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import org.jetbrains.annotations.NotNull;

public class SealFocusRecipe extends ShapelessRecipe {
    public static final SimpleRecipeSerializer<SealFocusRecipe> SERIALIZER =
        new SimpleRecipeSerializer<>(SealFocusRecipe::new);

    private static ItemStack getSealedStack() {
        ItemStack output = new ItemStack(HexItems.FOCUS::get);
        output.getOrCreateTag().putBoolean(ItemFocus.TAG_SEALED, true);
        return output;
    }

    private static NonNullList<Ingredient> createIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(2);
        ingredients.add(UnsealedIngredient.of(new ItemStack(HexItems.FOCUS::get)));
        ingredients.add(Ingredient.of(Items.HONEYCOMB));
        return ingredients;
    }

    public SealFocusRecipe(ResourceLocation id) {
        super(id, "", getSealedStack(), createIngredients());
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer inv) {
        ItemStack out = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.is(HexItems.FOCUS.get())) {
                out = stack.copy();
                break;
            }
        }

        if (!out.isEmpty()) {
            out.getOrCreateTag().putBoolean(ItemFocus.TAG_SEALED, true);
            out.setCount(1);
        }

        return out;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

