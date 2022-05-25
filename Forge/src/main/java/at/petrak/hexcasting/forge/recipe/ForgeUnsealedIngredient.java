package at.petrak.hexcasting.forge.recipe;

import at.petrak.hexcasting.api.addldata.DataHolder;
import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.LegacySpellDatum;
import at.petrak.hexcasting.api.spell.Widget;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.NBTIngredient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class ForgeUnsealedIngredient extends AbstractIngredient {
    private final ItemStack stack;

    protected ForgeUnsealedIngredient(ItemStack stack) {
        super(Arrays.stream(DatumType.values())
            .filter((it) -> it != DatumType.EMPTY && it != DatumType.OTHER)
            .map((type) -> {
                ItemStack newStack = stack.copy();
                NBTHelper.putString(newStack, DataHolderItem.TAG_OVERRIDE_VISUALLY, LegacySpellDatum.tagForType(type));
                return new Ingredient.ItemValue(newStack);
            }));
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
            DataHolder holder = IXplatAbstractions.INSTANCE.findDataHolder(this.stack);
            if (holder != null) {
                return holder.readRawDatum() != null && holder.writeDatum(LegacySpellDatum.make(Widget.NULL), true);
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
        json.addProperty("type", Objects.toString(CraftingHelper.getID(NBTIngredient.Serializer.INSTANCE)));
        json.addProperty("item", Objects.toString(stack.getItem().getRegistryName()));
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
