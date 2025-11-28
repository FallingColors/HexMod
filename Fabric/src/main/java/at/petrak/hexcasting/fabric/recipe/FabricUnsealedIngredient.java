package at.petrak.hexcasting.fabric.recipe;

import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.tropheusj.serialization_hooks.ingredient.BaseCustomIngredient;
import io.github.tropheusj.serialization_hooks.ingredient.IngredientDeserializer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.xml.crypto.Data;
import java.util.Objects;
import java.util.stream.Stream;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class FabricUnsealedIngredient extends BaseCustomIngredient {
    public static final ResourceLocation ID = modLoc("unsealed");

    private final ItemStack stack;

    private static ItemStack createStack(ItemStack base) {
        ItemStack newStack = base.copy();
        CompoundTag tag = newStack.get(DataComponents.CUSTOM_DATA).copyTag();
        NBTHelper.putString(tag, "VisualOverride", "any");
        newStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return newStack;
    }

    protected FabricUnsealedIngredient(ItemStack stack) {
        super(Stream.of(new Ingredient.ItemValue(createStack(stack))));
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
    public IngredientDeserializer getDeserializer() {
        return Deserializer.INSTANCE;
    }

    public static Ingredient fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf) {
        return new FabricUnsealedIngredient(ItemStack.STREAM_CODEC.decode(friendlyByteBuf));
    }

    public static Ingredient fromJson(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return null;
        }

        JsonObject object = element.getAsJsonObject();

        if (object.has("type") && object.getAsJsonPrimitive("type").getAsString().equals(ID.toString())) {
            return new FabricUnsealedIngredient(ItemStack.CODEC.parse(JsonOps.INSTANCE, object).getOrThrow());
        }

        return null;
    }

    public static class Deserializer implements IngredientDeserializer {
        public static final Deserializer INSTANCE = new Deserializer();

        @Override
        public Ingredient fromNetwork(FriendlyByteBuf buffer) {
            return FabricUnsealedIngredient.fromNetwork((RegistryFriendlyByteBuf) buffer);
        }

        @Nullable
        @Override
        public Ingredient fromJson(JsonObject object) {
            return FabricUnsealedIngredient.fromJson(object);
        }
    }
}
