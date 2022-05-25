package at.petrak.hexcasting.datagen;

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
}
