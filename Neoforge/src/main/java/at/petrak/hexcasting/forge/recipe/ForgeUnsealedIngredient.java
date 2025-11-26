package at.petrak.hexcasting.forge.recipe;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.forge.lib.ForgeHexIngredientTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeUnsealedIngredient implements ICustomIngredient {
<<<<<<< HEAD
    public static final ResourceLocation ID = modLoc("unsealed");
=======
    public static final MapCodec<ForgeUnsealedIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        ItemStack.CODEC.fieldOf("stack").forGetter(ForgeUnsealedIngredient::getStack)
    ).apply(inst, ForgeUnsealedIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeUnsealedIngredient> STREAM_CODEC = ItemStack.STREAM_CODEC.map(
            ForgeUnsealedIngredient::of,
            ForgeUnsealedIngredient::getStack
    );
>>>>>>> refs/remotes/slava/devel/port-1.21

    private final ItemStack stack;

    private static ItemStack createStack(ItemStack base) {
        ItemStack newStack = base.copy();
        base.set(HexDataComponents.VISUAL_OVERRIDE, Optional.empty());
        return newStack;
    }

    protected ForgeUnsealedIngredient(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    /**
     * Creates a new ingredient matching the given stack
     */
    public static ForgeUnsealedIngredient of(ItemStack stack) {
        return new ForgeUnsealedIngredient(stack);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        }
        if (this.stack.getItem() == input.getItem() && this.stack.getDamageValue() == input.getDamageValue()) {
            ADIotaHolder holder = IXplatAbstractions.INSTANCE.findDataHolder(this.stack);
            if (holder != null) {
                return holder.readIota() != null && holder.writeIota(NullIota.INSTANCE, true);
            }
        }

        return false;
    }

    @Override
    public Stream<ItemStack> getItems() {
<<<<<<< HEAD
        return Stream.empty();
=======
        return Stream.of(createStack(stack));
>>>>>>> refs/remotes/slava/devel/port-1.21
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
<<<<<<< HEAD
        return null;
    }

    @Override
    public @NotNull IIngredientSerializer<? extends Ingredient> getSerializer() {
        return ForgeUnsealedIngredient.Serializer.INSTANCE;
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        // TODO: should this be Partial or Strict
        json.addProperty("type", Objects.toString(CraftingHelper.getID(PartialNBTIngredient.Serializer.INSTANCE)));
        json.addProperty("item", Objects.toString(ForgeRegistries.ITEMS.getKey(stack.getItem())));
        return json;
    }


    public static class Serializer implements IIngredientSerializer<ForgeUnsealedIngredient> {
        public static final ForgeUnsealedIngredient.Serializer INSTANCE = new ForgeUnsealedIngredient.Serializer();

        @Override
        public @NotNull ForgeUnsealedIngredient parse(FriendlyByteBuf buffer) {
            return new ForgeUnsealedIngredient(buffer.readItem());
        }

        @Override
        public @NotNull ForgeUnsealedIngredient parse(@NotNull JsonObject json) {
            return new ForgeUnsealedIngredient(CraftingHelper.getItemStack(json, true));
        }

        @Override
        public void write(FriendlyByteBuf buffer, ForgeUnsealedIngredient ingredient) {
            buffer.writeItem(ingredient.stack);
        }
=======
        return ForgeHexIngredientTypes.UNSEALED_INGREDIENT.get();
>>>>>>> refs/remotes/slava/devel/port-1.21
    }
}
