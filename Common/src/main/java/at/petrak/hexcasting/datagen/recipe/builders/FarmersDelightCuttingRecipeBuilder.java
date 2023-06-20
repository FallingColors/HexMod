package at.petrak.hexcasting.datagen.recipe.builders;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class FarmersDelightCuttingRecipeBuilder implements RecipeBuilder {
    private String group = "";
    private final List<ProcessingOutput> outputs = Lists.newArrayList();
    private Ingredient input;
    private FarmersDelightToolIngredient toolAction;
    private SoundEvent sound;

    @Override
    public FarmersDelightCuttingRecipeBuilder unlockedBy(String s, CriterionTriggerInstance criterionTriggerInstance) {
        return this;
    }

    @Override
    public @NotNull FarmersDelightCuttingRecipeBuilder group(@Nullable String name) {
        group = name;
        return this;
    }

    @Override
    public Item getResult() {
        return Items.AIR; // Irrelevant, we implement serialization ourselves
    }

    public FarmersDelightCuttingRecipeBuilder withInput(ItemLike item) {
        return withInput(Ingredient.of(item));
    }

    public FarmersDelightCuttingRecipeBuilder withInput(ItemStack stack) {
        return withInput(Ingredient.of(stack));
    }

    public FarmersDelightCuttingRecipeBuilder withInput(Ingredient ingredient) {
        this.input = ingredient;
        return this;
    }

    public FarmersDelightCuttingRecipeBuilder withOutput(ItemLike output) {
        return withOutput(1f, output, 1);
    }

    public FarmersDelightCuttingRecipeBuilder withOutput(float chance, ItemLike output) {
        return withOutput(chance, output, 1);
    }

    public FarmersDelightCuttingRecipeBuilder withOutput(ItemLike output, int count) {
        return withOutput(1f, output, count);
    }

    public FarmersDelightCuttingRecipeBuilder withOutput(float chance, ItemLike output, int count) {
        return withOutput(new ItemStack(output, count), chance);
    }

    public FarmersDelightCuttingRecipeBuilder withOutput(ItemStack output, float chance) {
        this.outputs.add(new ItemProcessingOutput(output, chance));
        return this;
    }

    public FarmersDelightCuttingRecipeBuilder withOutput(String name) {
        return withOutput(1f, name, 1);
    }

    public FarmersDelightCuttingRecipeBuilder withOutput(String name, int count) {
        return withOutput(1f, name, count);
    }

    public FarmersDelightCuttingRecipeBuilder withOutput(float chance, String name) {
        return withOutput(chance, name, 1);
    }

    public FarmersDelightCuttingRecipeBuilder withOutput(float chance, String name, int count) {
        this.outputs.add(new CompatProcessingOutput(name, count, chance));
        return this;
    }

    public FarmersDelightCuttingRecipeBuilder withTool(FarmersDelightToolIngredient ingredient) {
        this.toolAction = ingredient;
        return this;
    }

    public FarmersDelightCuttingRecipeBuilder withSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    @Override
    public void save(Consumer<FinishedRecipe> consumer, ResourceLocation resourceLocation) {
        consumer.accept(new CuttingRecipe(resourceLocation));
    }

    public class CuttingRecipe implements FinishedRecipe {

        private final ResourceLocation id;

        public CuttingRecipe(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public void serializeRecipeData(@NotNull JsonObject json) {
            json.addProperty("type", "farmersdelight:cutting");

            if (!group.isEmpty()) {
                json.addProperty("group", group);
            }

            JsonArray jsonIngredients = new JsonArray();
            JsonArray jsonOutputs = new JsonArray();

            jsonIngredients.add(input.toJson());

            outputs.forEach(o -> jsonOutputs.add(o.serialize()));

            json.add("ingredients", jsonIngredients);
            json.add("tool", toolAction.serialize());
            json.add("result", jsonOutputs);

            if (sound != null) {
                json.addProperty("sound", BuiltInRegistries.SOUND_EVENT.getKey(sound).toString());
            }
        }

        @Override
        public @NotNull ResourceLocation getId() {
            return id;
        }

        @Override
        public @NotNull RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE; // Irrelevant, we implement serialization ourselves
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
