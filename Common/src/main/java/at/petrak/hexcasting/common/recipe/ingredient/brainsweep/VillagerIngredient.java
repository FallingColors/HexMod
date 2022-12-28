package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Special case for villagers so we can have biome/profession/level reqs
 */
public class VillagerIngredient extends BrainsweepeeIngredient {
	public final @Nullable ResourceLocation profession;
	public final @Nullable ResourceLocation biome;
	public final int minLevel;

	public VillagerIngredient(
		@Nullable ResourceLocation profession,
		@Nullable ResourceLocation biome,     // aka their "type"
		int minLevel
	) {
		this.profession = profession;
		this.biome = biome;
		this.minLevel = minLevel;
	}

	@Override
	public boolean test(Entity entity, ServerLevel level) {
		if (!(entity instanceof Villager villager)) return false;

		var data = villager.getVillagerData();
		ResourceLocation profID = IXplatAbstractions.INSTANCE.getID(data.getProfession());

		return (this.profession == null || this.profession.equals(profID))
			&& (this.biome == null || this.biome.equals(Registry.VILLAGER_TYPE.getKey(data.getType())))
			&& this.minLevel <= data.getLevel();
	}

	@Override
	public Entity exampleEntity(ClientLevel level) {
		var type = this.biome == null
			? VillagerType.PLAINS
			: Registry.VILLAGER_TYPE.get(this.biome);
		var out = new Villager(EntityType.VILLAGER, level, type);

		var profession = this.profession == null
			? VillagerProfession.TOOLSMITH
			: Registry.VILLAGER_PROFESSION.get(this.profession);
		out.getVillagerData().setProfession(profession);
		out.getVillagerData().setLevel(Math.max(this.minLevel, 1));
		return out;
	}

	@Override
	public List<Component> getTooltip(boolean advanced) {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(name());

		if (advanced) {
			if (minLevel >= 5) {
				tooltip.add(Component.translatable("hexcasting.tooltip.brainsweep.level", 5)
					.withStyle(ChatFormatting.DARK_GRAY));
			} else if (minLevel > 1) {
				tooltip.add(Component.translatable("hexcasting.tooltip.brainsweep.min_level", minLevel)
					.withStyle(ChatFormatting.DARK_GRAY));
			}

			if (biome != null) {
				tooltip.add(Component.literal(biome.toString()).withStyle(ChatFormatting.DARK_GRAY));
			}

			ResourceLocation displayId = Objects.requireNonNullElseGet(profession,
				() -> Registry.ENTITY_TYPE.getKey(EntityType.VILLAGER));
			tooltip.add(Component.literal(displayId.toString()).withStyle(ChatFormatting.DARK_GRAY));
		}

		tooltip.add(getModNameComponent());

		return tooltip;
	}

	public Component name() {
		MutableComponent component = Component.literal("");

		boolean addedAny = false;

		if (minLevel >= 5) {
			component.append(Component.translatable("merchant.level.5"));
			addedAny = true;
		} else if (minLevel > 1) {
			component.append(Component.translatable("merchant.level." + minLevel));
			addedAny = true;
		} else if (profession != null) {
			component.append(Component.translatable("merchant.level.1"));
			addedAny = true;
		}

		if (biome != null) {
			if (addedAny) {
				component.append(" ");
			}
			component.append(Component.translatable("biome.minecraft." + biome.getPath()));
			addedAny = true;
		}

		if (profession != null) {
			// We've for sure added something
			component.append(" ");
			component.append(Component.translatable("entity.minecraft.villager." + profession.getPath()));
		} else {
			if (addedAny) {
				component.append(" ");
			}
			component.append(EntityType.VILLAGER.getDescription());
		}

		return component;
	}

	public Component getModNameComponent() {
		String namespace = profession == null ? "minecraft" : profession.getNamespace();
		String mod = IXplatAbstractions.INSTANCE.getModName(namespace);
		return Component.literal(mod).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC);
	}

	@Override
	public JsonObject serialize() {
		var obj = new JsonObject();
		obj.addProperty("type", Type.VILLAGER.getSerializedName());

		if (this.profession != null) {
			obj.addProperty("profession", this.profession.toString());
		}
		if (this.biome != null) {
			obj.addProperty("biome", this.biome.toString());
		}
		obj.addProperty("minLevel", this.minLevel);
		return obj;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		if (this.profession != null) {
			buf.writeVarInt(1);
			buf.writeResourceLocation(this.profession);
		} else {
			buf.writeVarInt(0);
		}
		if (this.biome != null) {
			buf.writeVarInt(1);
			buf.writeResourceLocation(this.biome);
		} else {
			buf.writeVarInt(0);
		}
		buf.writeInt(this.minLevel);
	}

	public static VillagerIngredient deserialize(JsonObject json) {
		ResourceLocation profession = null;
		if (json.has("profession") && !json.get("profession").isJsonNull()) {
			profession = new ResourceLocation(GsonHelper.getAsString(json, "profession"));
		}
		ResourceLocation biome = null;
		if (json.has("biome") && !json.get("biome").isJsonNull()) {
			biome = new ResourceLocation(GsonHelper.getAsString(json, "biome"));
		}
		int minLevel = GsonHelper.getAsInt(json, "minLevel");
		int cost = GsonHelper.getAsInt(json, "cost");
		return new VillagerIngredient(profession, biome, minLevel);
	}

	public static VillagerIngredient read(FriendlyByteBuf buf) {
		ResourceLocation profession = null;
		var hasProfession = buf.readVarInt();
		if (hasProfession != 0) {
			profession = buf.readResourceLocation();
		}
		ResourceLocation biome = null;
		var hasBiome = buf.readVarInt();
		if (hasBiome != 0) {
			biome = buf.readResourceLocation();
		}
		int minLevel = buf.readInt();
		return new VillagerIngredient(profession, biome, minLevel);
	}
}
