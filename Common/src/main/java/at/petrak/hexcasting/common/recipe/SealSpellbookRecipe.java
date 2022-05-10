package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.common.items.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;

public class SealSpellbookRecipe extends CustomRecipe {
    public static final SimpleRecipeSerializer<SealSpellbookRecipe> SERIALIZER =
        new SimpleRecipeSerializer<>(SealSpellbookRecipe::new);

    public SealSpellbookRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        var foundWax = false;
        var foundOkSpellbook = false;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(HexItems.SPELLBOOK)) {
                    if (foundOkSpellbook) {
                        return false;
                    }

                    if (!ItemSpellbook.IsSealed(stack)) {
                        foundOkSpellbook = true;
                    } else {
                        return false;
                    }
                } else if (stack.is(Items.HONEYCOMB)) {
                    if (foundWax) {
                        return false;
                    }
                    foundWax = true;
                } else {
                    return false;
                }
            }
        }

        return foundWax && foundOkSpellbook;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        ItemStack out = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.is(HexItems.SPELLBOOK)) {
                out = stack.copy();
                break;
            }
        }

        if (!out.isEmpty()) {
            ItemSpellbook.SetSealed(out, true);
            out.setCount(1);
        }
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

