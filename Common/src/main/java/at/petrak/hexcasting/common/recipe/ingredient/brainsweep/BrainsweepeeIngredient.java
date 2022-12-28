package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.JsonObject;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

// Partially based on:
// https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/recipe/ingredient/EntityIngredient.java
// Licensed under MIT
public abstract class BrainsweepeeIngredient {
	public abstract boolean test(Entity entity, ServerLevel level);

	public abstract List<Component> getTooltip(boolean advanced);

	public abstract JsonObject serialize();

	public abstract void write(FriendlyByteBuf buf);

	/**
	 * For the benefit of showing to the client, return an example of the entity.
	 * <p>
	 * Can return null in case someone did something stupid with a recipe
	 */
	@Nullable
	public abstract Entity exampleEntity(ClientLevel level);

	public static BrainsweepeeIngredient read(FriendlyByteBuf buf) {
		var type = buf.readVarInt();
		return switch (Type.values()[type]) {
			case VILLAGER -> VillagerIngredient.read(buf);
			case ENTITY_TYPE -> EntityTypeIngredient.read(buf);
			case ENTITY_TAG -> EntityTagIngredient.read(buf);
		};
	}

	public static BrainsweepeeIngredient deserialize(JsonObject json) {
		var typestr = GsonHelper.getAsString(json, "type");
		var type = Type.valueOf(typestr.toUpperCase(Locale.ROOT));
		return switch (type) {
			case VILLAGER -> VillagerIngredient.deserialize(json);
			case ENTITY_TYPE -> EntityTypeIngredient.deserialize(json);
			case ENTITY_TAG -> EntityTagIngredient.deserialize(json);
		};
	}

	// TODO: make this a registry?
	public enum Type implements StringRepresentable {
		VILLAGER,
		ENTITY_TYPE,
		ENTITY_TAG;

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

}
