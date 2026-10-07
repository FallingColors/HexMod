package at.petrak.hexcasting.datagen;

import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface IXplatConditionsBuilder extends RecipeBuilder {
    IXplatConditionsBuilder whenModLoaded(String modid);

    IXplatConditionsBuilder whenModMissing(String modid);

    IXplatConditionsBuilder whenTagEmpty(TagKey<Item> tag);

    IXplatConditionsBuilder whenTagPopulated(TagKey<Item> tag);
}
