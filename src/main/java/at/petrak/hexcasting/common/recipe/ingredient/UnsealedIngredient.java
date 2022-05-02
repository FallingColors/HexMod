package at.petrak.hexcasting.common.recipe.ingredient;

import at.petrak.hexcasting.api.cap.DataHolder;
import at.petrak.hexcasting.api.cap.HexCapabilities;
import at.petrak.hexcasting.api.item.DataHolderItem;
import at.petrak.hexcasting.api.spell.DatumType;
import at.petrak.hexcasting.api.spell.SpellDatum;
import at.petrak.hexcasting.api.spell.Widget;
import at.petrak.hexcasting.api.utils.NBTHelper;
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
import java.util.Optional;

public class UnsealedIngredient extends AbstractIngredient {
	private final ItemStack stack;

	protected UnsealedIngredient(ItemStack stack) {
		super(Arrays.stream(DatumType.values())
			.filter((it) -> it != DatumType.EMPTY && it != DatumType.OTHER)
			.map((type) -> {
				ItemStack newStack = stack.copy();
				NBTHelper.putString(newStack, DataHolderItem.TAG_OVERRIDE_VISUALLY, SpellDatum.GetTagName(type));
				return new Ingredient.ItemValue(newStack);
			}));
		this.stack = stack;
	}

	/**
	 * Creates a new ingredient matching the given stack
	 */
	public static UnsealedIngredient of(ItemStack stack) {
		return new UnsealedIngredient(stack);
	}

	@Override
	public boolean test(@Nullable ItemStack input) {
		if (input == null)
			return false;
		if(this.stack.getItem() == input.getItem() && this.stack.getDamageValue() == input.getDamageValue()) {
			Optional<DataHolder> holder = HexCapabilities.getCapability(input, HexCapabilities.DATUM);
			if (holder.isPresent()) {
				return holder.get().readRawDatum() != null && holder.get().writeDatum(SpellDatum.make(Widget.NULL), true);
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
		return UnsealedIngredient.Serializer.INSTANCE;
	}

	@Override
	public @NotNull JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", Objects.toString(CraftingHelper.getID(NBTIngredient.Serializer.INSTANCE)));
		json.addProperty("item", Objects.toString(stack.getItem().getRegistryName()));
		return json;
	}


	public static class Serializer implements IIngredientSerializer<UnsealedIngredient> {
		public static final UnsealedIngredient.Serializer INSTANCE = new UnsealedIngredient.Serializer();

		@Override
		public @NotNull UnsealedIngredient parse(FriendlyByteBuf buffer) {
			return new UnsealedIngredient(buffer.readItem());
		}

		@Override
		public @NotNull UnsealedIngredient parse(@NotNull JsonObject json) {
			return new UnsealedIngredient(CraftingHelper.getItemStack(json, true));
		}

		@Override
		public void write(FriendlyByteBuf buffer, UnsealedIngredient ingredient) {
			buffer.writeItem(ingredient.stack);
		}
	}
}
