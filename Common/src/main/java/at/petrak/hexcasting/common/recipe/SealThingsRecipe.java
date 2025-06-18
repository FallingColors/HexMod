package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.items.storage.ItemFocus;
import at.petrak.hexcasting.common.items.storage.ItemSpellbook;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class SealThingsRecipe extends CustomRecipe {
    public final Sealee sealee;

    public static final SimpleCraftingRecipeSerializer<SealThingsRecipe> FOCUS_SERIALIZER =
        new SimpleCraftingRecipeSerializer<>(SealThingsRecipe::focus);
    public static final SimpleCraftingRecipeSerializer<SealThingsRecipe> SPELLBOOK_SERIALIZER =
        new SimpleCraftingRecipeSerializer<>(SealThingsRecipe::spellbook);

    public SealThingsRecipe(CraftingBookCategory category, Sealee sealee) {
        super(category);
        this.sealee = sealee;
    }


    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        boolean foundComb = false;
        boolean foundSealee = false;

        for (int i = 0; i < container.size(); i++) {
            var stack = container.getItem(i);
            if (this.sealee.isCorrectSealee(stack)) {
                if (foundSealee) return false;
                foundSealee = true;
            } else if (stack.is(HexTags.Items.SEAL_MATERIALS)) {
                if (foundComb) return false;
                foundComb = true;
            }
        }

        return foundComb && foundSealee;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput inv, HolderLookup.RegistryLookup.@NotNull Provider registryProvider) {
        ItemStack sealee = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getItem(i);
            if (this.sealee.isCorrectSealee(stack)) {
                sealee = stack.copy();
                break;
            }
        }

        if (!sealee.isEmpty()) {
            this.sealee.seal(sealee);
            sealee.setCount(1);
        }

        return sealee;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return switch (this.sealee) {
            case FOCUS -> FOCUS_SERIALIZER;
            case SPELLBOOK -> SPELLBOOK_SERIALIZER;
        };
    }

    public static SealThingsRecipe focus(CraftingBookCategory category) {
        return new SealThingsRecipe(category, Sealee.FOCUS);
    }

    public static SealThingsRecipe spellbook(CraftingBookCategory category) {
        return new SealThingsRecipe(category, Sealee.SPELLBOOK);
    }

    public enum Sealee implements StringRepresentable {
        FOCUS,
        SPELLBOOK;

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public boolean isCorrectSealee(ItemStack stack) {
            return switch (this) {
                case FOCUS -> stack.is(HexItems.FOCUS)
                    && stack.has(HexDataComponents.IOTA)
                    && !ItemFocus.isSealed(stack);
                case SPELLBOOK -> stack.is(HexItems.SPELLBOOK)
                    && HexItems.SPELLBOOK.readIota(stack) != null
                    && !ItemSpellbook.isSealed(stack);
            };
        }

        public void seal(ItemStack stack) {
            switch (this) {
                case FOCUS -> {
                    ItemFocus.seal(stack);
                }
                case SPELLBOOK -> {
                    ItemSpellbook.setSealed(stack, true);
                }
            }
        }
    }
}

