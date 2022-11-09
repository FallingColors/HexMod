package at.petrak.hexcasting.datagen.recipe.builders;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public class CompatIngredientValue implements Ingredient.Value {
    public final String item;

    public CompatIngredientValue(String name) {
        this.item = name;
    }

    public @NotNull Collection<ItemStack> getItems() {
        return Collections.emptyList();
    }

    public @NotNull JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("item", item);
        return jsonObject;
    }

    public static Ingredient of(String itemName) {
        return new Ingredient(Stream.of(new CompatIngredientValue(itemName)));
    }
}

