package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class EntityTagIngredient extends BrainsweepeeIngredient {
	public final TagKey<EntityType<?>> entityTypeTag;

	public EntityTagIngredient(TagKey<EntityType<?>> tag) {
		this.entityTypeTag = tag;
	}

	@Override
	public boolean test(Entity entity, ServerLevel level) {
		return entity.getType().is(this.entityTypeTag);
	}

	@Override
	public List<Component> getTooltip(boolean advanced) {
		String key = "tag."
			+ this.entityTypeTag.location().getNamespace()
			+ "."
			+ this.entityTypeTag.location().getPath().replace('/', '.');
		boolean moddersDidAGoodJob = I18n.exists(key);

		var out = new ArrayList<Component>();
		out.add(moddersDidAGoodJob
			? Component.translatable(key)
			: Component.literal("#" + entityTypeTag));
		if (advanced && moddersDidAGoodJob) {
			out.add(Component.literal("#" + entityTypeTag).withStyle(ChatFormatting.DARK_GRAY));
		}
		return out;
	}

	@Override
	public Entity exampleEntity(ClientLevel level) {
		var someEntityTys = Registry.ENTITY_TYPE.getTagOrEmpty(this.entityTypeTag).iterator();
		if (someEntityTys.hasNext()) {
			var someTy = someEntityTys.next();
			return someTy.value().create(level);
		} else {
			return null;
		}
	}

	@Override
	public JsonObject serialize() {
		var obj = new JsonObject();
		obj.addProperty("type", Type.ENTITY_TAG.getSerializedName());

		obj.addProperty("tag", this.entityTypeTag.location().toString());

		return obj;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(Type.ENTITY_TAG.ordinal());

		buf.writeResourceLocation(this.entityTypeTag.location());
	}

	public static EntityTagIngredient deserialize(JsonObject obj) {
		var typeLoc = ResourceLocation.tryParse(GsonHelper.getAsString(obj, "entityType"));
		var type = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, typeLoc);
		return new EntityTagIngredient(type);
	}

	public static EntityTagIngredient read(FriendlyByteBuf buf) {
		var typeLoc = buf.readResourceLocation();
		var type = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, typeLoc);
		return new EntityTagIngredient(type);
	}
}
