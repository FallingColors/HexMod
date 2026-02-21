package at.petrak.hexcasting.fabric.datagen;

import at.petrak.hexcasting.datagen.IXplatConditionsBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.Criterion;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HexFabricConditionsBuilder implements IXplatConditionsBuilder {
    private final List<ResourceCondition> conditions = new ArrayList<>();
    private final RecipeBuilder parent;

    public HexFabricConditionsBuilder(RecipeBuilder parent) {
        this.parent = parent;
    }

    @Override
    public IXplatConditionsBuilder whenModLoaded(String modid) {
        conditions.add(ResourceConditions.allModsLoaded(modid));
        return this;
    }

    @Override
    public IXplatConditionsBuilder whenModMissing(String modid) {
        conditions.add(ResourceConditions.not(ResourceConditions.allModsLoaded(modid)));
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(@NotNull String string, @NotNull Criterion criterionTriggerInstance) {
        return parent.unlockedBy(string, criterionTriggerInstance);
    }

    @Override
    public RecipeBuilder group(@Nullable String string) {
        return parent.group(string);
    }

    @Override
    public Item getResult() {
        return parent.getResult();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void save(@NotNull RecipeOutput consumer, @NotNull ResourceLocation resourceLocation) {
        ResourceCondition[] array = conditions.toArray(ResourceCondition[]::new);

        RecipeOutput withConditions = new RecipeOutput() {
            @Override
            public void accept(ResourceLocation resourceLocation, Recipe<?> recipe, @Nullable AdvancementHolder advancementHolder) {
                FabricDataGenHelper.addConditions(consumer, array);
                consumer.accept(resourceLocation, recipe, advancementHolder);
            }

            @Override
            public Advancement.Builder advancement() {
                return consumer.advancement();
            }
        };

        parent.save(withConditions, resourceLocation);
    }
}
