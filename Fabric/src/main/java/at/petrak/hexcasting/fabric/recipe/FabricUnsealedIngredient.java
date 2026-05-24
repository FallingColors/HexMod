package at.petrak.hexcasting.fabric.recipe;

import at.petrak.hexcasting.api.utils.NBTHelper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class FabricUnsealedIngredient extends Ingredient implements CustomIngredient {
    public static final ResourceLocation ID = modLoc("unsealed");

    private final ItemStack stack;

    public static final MapCodec<FabricUnsealedIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(FabricUnsealedIngredient::getStack)
    ).apply(instance, FabricUnsealedIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FabricUnsealedIngredient> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, FabricUnsealedIngredient::getId,
            ItemStack.STREAM_CODEC, FabricUnsealedIngredient::getStack,
            (a, b) -> new FabricUnsealedIngredient(b)
    );

    private static ItemStack createStack(ItemStack base) {
        ItemStack newStack = base.copy();
        CompoundTag tag = newStack.get(DataComponents.CUSTOM_DATA).copyTag();
        NBTHelper.putString(tag, "VisualOverride", "any");
        newStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return newStack;
    }

    protected FabricUnsealedIngredient(ItemStack stack) {
        super(Arrays.stream(Ingredient.of(stack).values));
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    public ResourceLocation getId() {
        return ID;
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
    public List<ItemStack> getMatchingStacks() {
        return List.of(stack);
    }

    @Override
    public boolean requiresTesting() {
        return false;
    }

    @Override
    public CustomIngredientSerializer getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements CustomIngredientSerializer {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public ResourceLocation getIdentifier() {
            return FabricUnsealedIngredient.ID;
        }

        @Override
        public MapCodec getCodec(boolean b) {
            return CODEC;
        }

        @Override
        public StreamCodec getPacketCodec() {
            return STREAM_CODEC;
        }
    }
}
