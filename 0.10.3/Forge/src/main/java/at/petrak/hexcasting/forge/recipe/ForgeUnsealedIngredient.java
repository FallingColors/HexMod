package at.petrak.hexcasting.forge.recipe;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.spell.iota.NullIota;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeUnsealedIngredient extends AbstractIngredient {
    public static final ResourceLocation ID = modLoc("unsealed");

    private final ItemStack stack;

    private static ItemStack createStack(ItemStack base) {
        ItemStack newStack = base.copy();
        NBTHelper.putString(newStack, IotaHolderItem.TAG_OVERRIDE_VISUALLY, "any");
        return newStack;
    }

    protected ForgeUnsealedIngredient(ItemStack stack) {
        super(Stream.of(new Ingredient.ItemValue(createStack(stack))));
        this.stack = stack;
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
                return holder.readIotaTag() != null && holder.writeIota(new NullIota(), true);
            }
        }

        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
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
        json.addProperty("item", Objects.toString(Registry.ITEM.getKey(stack.getItem())));
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
    }
}
