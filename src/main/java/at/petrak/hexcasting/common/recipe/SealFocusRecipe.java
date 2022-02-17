package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.common.items.ItemFocus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;

public class SealFocusRecipe extends CustomRecipe {
    public static final SimpleRecipeSerializer<SealFocusRecipe> SERIALIZER =
        new SimpleRecipeSerializer<>(SealFocusRecipe::new);

    public SealFocusRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        var foundWax = false;
        var foundOkFocus = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                var item = stack.getItem();
                if (item instanceof ItemFocus) {
                    if (foundOkFocus) {
                        return false;
                    }

                    if (stack.hasTag()
                        && stack.getTag().contains(ItemFocus.TAG_DATA)
                        && (!stack.getTag().contains(ItemFocus.TAG_SEALED)
                        || !stack.getTag().getBoolean(ItemFocus.TAG_SEALED))) {
                        foundOkFocus = true;
                    } else {
                        return false;
                    }
                } else if (item == Items.HONEYCOMB) {
                    if (foundWax) {
                        return false;
                    }
                    foundWax = true;
                } else {
                    return false;
                }
            }
        }

        return foundWax && foundOkFocus;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack out = null;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() != Items.HONEYCOMB) {
                out = stack.copy();
                break;
            }
        }
        out.getTag().putBoolean(ItemFocus.TAG_SEALED, true);
        out.setCount(1);
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}

