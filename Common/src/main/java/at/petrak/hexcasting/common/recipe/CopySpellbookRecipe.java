package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CopySpellbookRecipe extends CustomRecipe {
    public static final SimpleCraftingRecipeSerializer<CopySpellbookRecipe> SERIALIZER =
        new SimpleCraftingRecipeSerializer<>(CopySpellbookRecipe::new);

    public CopySpellbookRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        boolean foundBreath = false;
        boolean foundOriginal = false;
        boolean foundBlank = false;

        for (int i = 0; i < container.size(); i++) {
            var stack = container.getItem(i);
            if (stack.is(Items.DRAGON_BREATH)) {
                if (foundBreath) return false;
                foundBreath = true;
            } else if (stack.is(HexItems.SPELLBOOK.get())) {
                if (stack.has(HexDataComponents.SPELLBOOK_PAGES.get())) {
                    if (foundOriginal) return false;
                    foundOriginal = true;
                } else {
                    if (foundBlank) return false;
                    foundBlank = true;
                }
            } else {
                return false;
            }
        }

        return foundBreath && foundOriginal && foundBlank;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput inv, HolderLookup.RegistryLookup.@NotNull Provider registryProvider) {
        ItemStack output = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getItem(i);
            if (stack.is(HexItems.SPELLBOOK.get()) && stack.has(HexDataComponents.SPELLBOOK_PAGES.get())) {
                output = stack.copy();
                break;
            }
        }

        if (!output.isEmpty()) {
            output.setCount(1);
        }

        return output;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput craftingInput) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);

        for(int i = 0; i < remainingItems.size(); ++i) {
            ItemStack stack = craftingInput.getItem(i);
            if (stack.getItem().hasCraftingRemainingItem()) {
                remainingItems.set(i, new ItemStack(stack.getItem().getCraftingRemainingItem()));
            } else if (stack.is(HexItems.SPELLBOOK.get()) && stack.get(HexDataComponents.SPELLBOOK_PAGES.get()) != null) {
                remainingItems.set(i, stack.copyWithCount(1));
            }
        }

        return remainingItems;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
