package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class EraseSpellbookRecipe extends CustomRecipe {
    public static final SimpleCraftingRecipeSerializer<EraseSpellbookRecipe> SERIALIZER =
            new SimpleCraftingRecipeSerializer<>(EraseSpellbookRecipe::new);

    public EraseSpellbookRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        boolean foundEraseMaterial = false;
        boolean foundSpellbook = false;

        for (int i = 0; i < container.size(); i++) {
            var stack = container.getItem(i);
            if (stack.is(HexTags.Items.SPELLBOOK_ERASE_MATERIALS)) {
                if (foundEraseMaterial) return false;
                foundEraseMaterial = true;
            } else if (stack.is(HexItems.SPELLBOOK.get()) && stack.has(HexDataComponents.SPELLBOOK_PAGES.get())) {
                if (foundSpellbook) return false;
                foundSpellbook = true;
            } else {
                return false;
            }
        }

        return foundEraseMaterial && foundSpellbook;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput inv, HolderLookup.RegistryLookup.@NotNull Provider registryProvider) {
        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getItem(i);
            if (stack.is(HexItems.SPELLBOOK.get()) && stack.has(HexDataComponents.SPELLBOOK_PAGES.get())) {
                var output = new ItemStack(HexItems.SPELLBOOK.get(), 1);
                var variant = stack.get(HexDataComponents.ITEM_VARIANT.get());
                if (variant != null) {
                    output.set(HexDataComponents.ITEM_VARIANT.get(), variant);
                }
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
