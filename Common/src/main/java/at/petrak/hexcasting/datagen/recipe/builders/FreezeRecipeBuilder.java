package at.petrak.hexcasting.datagen.recipe.builders;

import at.petrak.hexcasting.common.recipe.FreezeRecipe;
import at.petrak.hexcasting.common.recipe.ingredient.state.StateIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class FreezeRecipeBuilder implements RecipeBuilder {
    private final StateIngredient blockIn;
    private final BlockState result;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    public FreezeRecipeBuilder(StateIngredient blockIn, BlockState result) {
        this.blockIn = blockIn;
        this.result = result;
    }

    @Override
    public RecipeBuilder unlockedBy(String pCriterionName, Criterion<?> pCriterionTrigger) {
        criteria.put(pCriterionName, pCriterionTrigger);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String pGroupName) {
        return this;
    }

    @Override
    public Item getResult() {
        return this.result.getBlock().asItem();
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);

        var recipe = new FreezeRecipe(blockIn, result);
        recipeOutput.accept(id.withPrefix("freeze/"), recipe, advancement.build(id.withPrefix("recipes/freeze/")));
    }
}
