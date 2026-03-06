package at.petrak.hexcasting.forge.datagen;

import at.petrak.hexcasting.datagen.IXplatConditionsBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

/**
 * Injects neoforge:conditions into recipe JSON, matching the NeoForge 1.21.1
 * data load conditions format. Replaces the removed ConditionalRecipe API.
 */
public class ForgeHexConditionsBuilder implements IXplatConditionsBuilder {
    private final List<JsonObject> conditionJsons = new ArrayList<>();
    private final RecipeBuilder parent;

    public ForgeHexConditionsBuilder(RecipeBuilder parent) {
        this.parent = parent;
    }

    @Override
    public IXplatConditionsBuilder whenModLoaded(String modid) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "neoforge:mod_loaded");
        json.addProperty("modid", modid);
        conditionJsons.add(json);
        return this;
    }

    @Override
    public IXplatConditionsBuilder whenModMissing(String modid) {
        JsonObject inner = new JsonObject();
        inner.addProperty("type", "neoforge:mod_loaded");
        inner.addProperty("modid", modid);
        JsonObject json = new JsonObject();
        json.addProperty("type", "neoforge:not");
        json.add("value", inner);
        conditionJsons.add(json);
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
        Consumer<FinishedRecipe> withConditions = json -> consumer.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(@NotNull JsonObject jsonObject) {
                json.serializeRecipeData(jsonObject);
                if (!conditionJsons.isEmpty()) {
                    JsonArray conditions = new JsonArray();
                    for (JsonObject cond : conditionJsons) {
                        conditions.add(cond);
                    }
                    jsonObject.add("neoforge:conditions", conditions);
                }
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

        parent.save(withConditions, resourceLocation);
    }
}
