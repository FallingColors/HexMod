package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.JsonObject;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class EntityTypeIngredient extends BrainsweepeeIngredient {
	public final EntityType<?> entityType;

	public EntityTypeIngredient(EntityType<?> entityType) {
		this.entityType = entityType;
	}

	@Override
	public boolean test(Entity entity, ServerLevel level) {
		// entity types are singletons
		return entity.getType() == this.entityType;
	}

	@Override
	public List<Component> getTooltip(boolean advanced) {
		return List.of(this.entityType.getDescription());
	}

	@Override
	public Entity exampleEntity(ClientLevel level) {
		return this.entityType.create(level);
	}

	@Override
	public JsonObject serialize() {
		var obj = new JsonObject();
		obj.addProperty("type", Type.ENTITY_TYPE.getSerializedName());
		obj.addProperty("entityType", Registry.ENTITY_TYPE.getKey(this.entityType).toString());

		return obj;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(Type.ENTITY_TYPE.ordinal());

		buf.writeVarInt(Registry.ENTITY_TYPE.getId(this.entityType));
	}

	public static EntityTypeIngredient deserialize(JsonObject obj) {
		var typeLoc = ResourceLocation.tryParse(GsonHelper.getAsString(obj, "entityType"));
		if (!Registry.ENTITY_TYPE.containsKey(typeLoc)) {
			throw new IllegalArgumentException("unknown entity type " + typeLoc);
		}
		return new EntityTypeIngredient(Registry.ENTITY_TYPE.get(typeLoc));
	}

	public static EntityTypeIngredient read(FriendlyByteBuf buf) {
		var tyId = buf.readVarInt();
		return new EntityTypeIngredient(Registry.ENTITY_TYPE.byId(tyId));
	}
}
