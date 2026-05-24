package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.datagen.IXplatConditionsBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        @NotNull Criterion<?> criterion) {
        return parent.unlockedBy(string, criterion);
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
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        var out = recipeOutput.withConditions(conditions.toArray(ICondition[]::new));

        parent.save(out, id);
    }
}
