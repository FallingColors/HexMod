package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.datagen.IXplatConditionsBuilder;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ForgeHexConditionsBuilder implements IXplatConditionsBuilder, IConditionBuilder {
    private final List<ICondition> conditions = new ArrayList<>();
    private final RecipeBuilder parent;

    public ForgeHexConditionsBuilder(RecipeBuilder parent) {
        this.parent = parent;
    }

    @Override
    public IXplatConditionsBuilder whenModLoaded(String modid) {
        conditions.add(modLoaded(modid));
        return this;
    }

    @Override
    public IXplatConditionsBuilder whenModMissing(String modid) {
        conditions.add(not(modLoaded(modid)));
        return this;
    }

    @Override
    public @NotNull RecipeBuilder unlockedBy(@NotNull String string,
        @NotNull CriterionTriggerInstance criterionTriggerInstance) {
        return parent.unlockedBy(string, criterionTriggerInstance);
    }

    @Override
    public @NotNull RecipeBuilder group(@Nullable String string) {
        return parent.group(string);
    }

    @Override
    public @NotNull Item getResult() {
        return parent.getResult();
    }

    @Override
    public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation resourceLocation) {
        var conditionalBuilder = ConditionalRecipe.builder();
        for (ICondition condition : conditions) {
            conditionalBuilder.addCondition(condition);
        }
        conditionalBuilder.addRecipe(recipeConsumer -> parent.save(recipeConsumer, resourceLocation))
            .build(consumer, resourceLocation);
    }
}
