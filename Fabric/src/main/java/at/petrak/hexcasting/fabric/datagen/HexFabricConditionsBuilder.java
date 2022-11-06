package at.petrak.hexcasting.fabric.datagen;

import at.petrak.hexcasting.datagen.IXplatConditionsBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HexFabricConditionsBuilder implements IXplatConditionsBuilder {
    private final List<ConditionJsonProvider> conditions = new ArrayList<>();
    private final RecipeBuilder parent;

    public HexFabricConditionsBuilder(RecipeBuilder parent) {
        this.parent = parent;
    }

    @Override
    public IXplatConditionsBuilder whenModLoaded(String modid) {
        conditions.add(DefaultResourceConditions.allModsLoaded(modid));
        return this;
    }

    @Override
    public IXplatConditionsBuilder whenModMissing(String modid) {
        conditions.add(DefaultResourceConditions.not(DefaultResourceConditions.allModsLoaded(modid)));
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(@NotNull String string, @NotNull CriterionTriggerInstance criterionTriggerInstance) {
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
    public void save(@NotNull Consumer<FinishedRecipe> consumer, @NotNull ResourceLocation resourceLocation) {
        Consumer<FinishedRecipe> withConditions = json -> {
            FabricDataGenHelper.addConditions(json, conditions.toArray(new ConditionJsonProvider[0]));

            consumer.accept(new FinishedRecipe() {
                @Override
                public void serializeRecipeData(@NotNull JsonObject jsonObject) {
                    json.serializeRecipeData(jsonObject);
                    ConditionJsonProvider[] conditions = FabricDataGenHelper.consumeConditions(json);
                    ConditionJsonProvider.write(jsonObject, conditions);
                }

                @Override
                public ResourceLocation getId() {
                    return json.getId();
                }

                @Override
                public RecipeSerializer<?> getType() {
                    return json.getType();
                }

                @Nullable
                @Override
                public JsonObject serializeAdvancement() {
                    return json.serializeAdvancement();
                }

                @Nullable
                @Override
                public ResourceLocation getAdvancementId() {
                    return json.getAdvancementId();
                }
            });
        };

        parent.save(withConditions, resourceLocation);
    }
}
