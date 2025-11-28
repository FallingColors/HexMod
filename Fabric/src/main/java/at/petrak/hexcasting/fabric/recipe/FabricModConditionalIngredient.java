package at.petrak.hexcasting.fabric.recipe;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import io.github.tropheusj.serialization_hooks.ingredient.BaseCustomIngredient;
import io.github.tropheusj.serialization_hooks.ingredient.IngredientDeserializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static at.petrak.hexcasting.api.HexAPI.modLoc;


public class FabricModConditionalIngredient extends BaseCustomIngredient {
    public static final ResourceLocation ID = modLoc("mod_conditional");

    private final Ingredient main;
    private final String modid;
    private final Ingredient ifModLoaded;

    private final Ingredient toUse;

    protected FabricModConditionalIngredient(Ingredient main, String modid, Ingredient ifModLoaded) {
        super(IXplatAbstractions.INSTANCE.isModPresent(modid) ? Arrays.stream(ifModLoaded.values) : Arrays.stream(main.values));
        this.main = main;
        this.modid = modid;
        this.ifModLoaded = ifModLoaded;

        this.toUse = IXplatAbstractions.INSTANCE.isModPresent(modid) ? ifModLoaded : main;
    }


    public static FabricModConditionalIngredient of(Ingredient main, String modid, Ingredient ifModLoaded) {
        return new FabricModConditionalIngredient(main, modid, ifModLoaded);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        return toUse.test(input);
    }

    @Override
    public IngredientDeserializer getDeserializer() {
        return Deserializer.INSTANCE;
    }

    public static Ingredient fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return Ingredient.CONTENTS_STREAM_CODEC.decode((RegistryFriendlyByteBuf) friendlyByteBuf); // Just send the actual ingredient
    }

    public static Ingredient fromJson(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonObject())
            return null;

        JsonObject object = element.getAsJsonObject();

        if (object.has("type") && object.getAsJsonPrimitive("type").getAsString().equals(ID.toString())) {
            if (object.has("modid") && IXplatAbstractions.INSTANCE.isModPresent(object.getAsJsonPrimitive("modid").getAsString())) {
                try {
                    Ingredient ingredient = Ingredient.CODEC.decode(JsonOps.INSTANCE, object.get("if_loaded")).getOrThrow().getFirst();
                    if (!ingredient.isEmpty()) {
                        return ingredient;
                    }
                } catch (JsonParseException e) {
                    // NO-OP
                }
            }

            return Ingredient.CODEC.decode(JsonOps.INSTANCE, object.get("default")).getOrThrow().getFirst();
        }

        return null;
    }

    public static class Deserializer implements IngredientDeserializer {
        public static final Deserializer INSTANCE = new Deserializer();

        @Override
        public Ingredient fromNetwork(FriendlyByteBuf buffer) {
            return FabricModConditionalIngredient.fromNetwork(buffer);
        }

        @Nullable
        @Override
        public Ingredient fromJson(JsonObject object) {
            return FabricModConditionalIngredient.fromJson(object);
        }
    }
}
