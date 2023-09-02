package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.datagen.recipe.builders.FarmersDelightToolIngredient;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;

public interface IXplatIngredients {
    Ingredient glowstoneDust();

    Ingredient leather();

    Ingredient ironNugget();

    Ingredient goldNugget();

    Ingredient copperIngot();

    Ingredient ironIngot();

    Ingredient goldIngot();

    EnumMap<DyeColor, Ingredient> dyes();

    Ingredient stick();

    Ingredient whenModIngredient(Ingredient defaultIngredient, String modid, Ingredient modIngredient);

    FarmersDelightToolIngredient axeStrip();

    FarmersDelightToolIngredient axeDig();
}
