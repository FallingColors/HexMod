package at.petrak.hexcasting.forge.recipe;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeModConditionalIngredient extends AbstractIngredient {
    public static final ResourceLocation ID = modLoc("mod_conditional");

    private final Ingredient main;
    private final String modid;
    private final Ingredient ifModLoaded;

    private final Ingredient toUse;

    protected ForgeModConditionalIngredient(Ingredient main, String modid, Ingredient ifModLoaded) {
        super(IXplatAbstractions.INSTANCE.isModPresent(modid) ? Arrays.stream(ifModLoaded.values) : Arrays.stream(main.values));
        this.main = main;
        this.modid = modid;
        this.ifModLoaded = ifModLoaded;

        this.toUse = IXplatAbstractions.INSTANCE.isModPresent(modid) ? ifModLoaded : main;
    }

    /**
     * Creates a new ingredient matching the given stack
     */
    public static ForgeModConditionalIngredient of(Ingredient main, String modid, Ingredient ifModLoaded) {
        return new ForgeModConditionalIngredient(main, modid, ifModLoaded);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        return toUse.test(input);
    }

    @Override
    public boolean isSimple() {
        return toUse.isSimple();
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", Objects.toString(ID));
        json.add("default", main.toJson());
        json.addProperty("modid", modid);
        json.add("if_loaded", ifModLoaded.toJson());
        return json;
    }

    @Override
    public @NotNull IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static @NotNull Ingredient fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return Ingredient.fromNetwork(friendlyByteBuf); // Just send the actual ingredient
    }

    public static Ingredient fromJson(JsonObject object) {
        if (object.has("type") && object.getAsJsonPrimitive("type").getAsString().equals(ID.toString())) {
            if (object.has("modid") && IXplatAbstractions.INSTANCE.isModPresent(object.getAsJsonPrimitive("modid").getAsString())) {
                try {
                    Ingredient ingredient = Ingredient.fromJson(object.get("if_loaded"));
                    if (!ingredient.isEmpty()) {
                        return ingredient;
                    }
                } catch (JsonParseException e) {
                    // NO-OP
                }
            }

            return Ingredient.fromJson(object.get("default"));
        }

        return Ingredient.of();
    }

    public static class Serializer implements IIngredientSerializer<Ingredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public @NotNull Ingredient parse(@NotNull FriendlyByteBuf buffer) {
            return fromNetwork(buffer);
        }

        @Override
        public @NotNull Ingredient parse(@NotNull JsonObject json) {
            return fromJson(json);
        }

        @Override
        public void write(@NotNull FriendlyByteBuf buffer, @NotNull Ingredient ingredient) {
            if (ingredient instanceof ForgeModConditionalIngredient conditionalIngredient)
                conditionalIngredient.toUse.toNetwork(buffer);
            // It shouldn't be possible to not be a ForgeModConditionalIngredient here
        }
    }
}
