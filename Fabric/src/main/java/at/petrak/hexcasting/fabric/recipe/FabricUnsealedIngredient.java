package at.petrak.hexcasting.fabric.recipe;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexIotaTypes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.tropheusj.serialization_hooks.ingredient.BaseCustomIngredient;
import io.github.tropheusj.serialization_hooks.ingredient.IngredientDeserializer;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class FabricUnsealedIngredient extends BaseCustomIngredient {
    private static final ResourceLocation ID = modLoc("unsealed");

    private final ItemStack stack;

    protected FabricUnsealedIngredient(ItemStack stack) {
        super(HexIotaTypes.REGISTRY.keySet().stream()
            .map(it -> {
                ItemStack newStack = stack.copy();
                NBTHelper.putString(newStack, IotaHolderItem.TAG_OVERRIDE_VISUALLY, it.toString());
                return new Ingredient.ItemValue(newStack);
            }));
        this.stack = stack;
    }

    /**
     * Creates a new ingredient matching the given stack
     */
    public static FabricUnsealedIngredient of(ItemStack stack) {
        return new FabricUnsealedIngredient(stack);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        }

        return false;
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Objects.toString(ID));
        json.addProperty("item", Objects.toString(Registry.ITEM.getKey(this.stack.getItem())));
        return json;
    }

    @Override
    public IngredientDeserializer getDeserializer() {
        return Deserializer.INSTANCE;
    }

    public static Ingredient fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return new FabricUnsealedIngredient(friendlyByteBuf.readItem());
    }

    public static Ingredient fromJson(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return null;
        }

        JsonObject object = element.getAsJsonObject();

        if (object.has("type") && object.getAsJsonPrimitive("type").getAsString().equals(ID.toString())) {
            return new FabricUnsealedIngredient(new ItemStack(ShapedRecipe.itemFromJson(object)));
        }

        return null;
    }

    @Override
    public void toNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeItem(stack);
    }

    public static class Deserializer implements IngredientDeserializer {
        public static final Deserializer INSTANCE = new Deserializer();

        @Override
        public Ingredient fromNetwork(FriendlyByteBuf buffer) {
            return FabricUnsealedIngredient.fromNetwork(buffer);
        }

        @Nullable
        @Override
        public Ingredient fromJson(JsonObject object) {
            return FabricUnsealedIngredient.fromJson(object);
        }
    }
}
