package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Special case for villagers so we can have biome/profession/level reqs
 */
public class VillagerIngredient extends BrainsweepeeIngredient {
    public final @Nullable VillagerProfession profession;
    public final @Nullable VillagerType biome;
    public final int minLevel;

    public VillagerIngredient(
        @Nullable VillagerProfession profession,
        @Nullable VillagerType biome,
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

        return (this.profession == null || this.profession.equals(data.getProfession()))
            && (this.biome == null || this.biome.equals(data.getType()))
            && this.minLevel <= data.getLevel();
    }

    @Override
    public Entity exampleEntity(Level level) {
        var biome = Objects.requireNonNullElse(this.biome, VillagerType.PLAINS);
        var profession = Objects.requireNonNullElse(this.profession, VillagerProfession.TOOLSMITH);
        var tradeLevel = Math.min(this.minLevel, 1);

        var out = new Villager(EntityType.VILLAGER, level);

        var data = out.getVillagerData();
        data
            .setProfession(profession)
            .setType(biome)
            .setLevel(tradeLevel);
        out.setVillagerData(data);

        // just random bullshit go to try and get it to update for god's sake
        var tag = new CompoundTag();
        out.save(tag);

        return out;
    }

    @Override
    public List<Component> getTooltip(boolean advanced) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(this.getName());

        if (advanced) {
            if (minLevel >= 5) {
                tooltip.add(Component.translatable("hexcasting.tooltip.brainsweep.level", 5)
                    .withStyle(ChatFormatting.DARK_GRAY));
            } else if (minLevel > 1) {
                tooltip.add(Component.translatable("hexcasting.tooltip.brainsweep.min_level", minLevel)
                    .withStyle(ChatFormatting.DARK_GRAY));
            }

            if (this.biome != null) {
                tooltip.add(Component.literal(this.biome.toString()).withStyle(ChatFormatting.DARK_GRAY));
            }

            if (this.profession != null) {
                tooltip.add(Component.literal(this.profession.toString()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        tooltip.add(BrainsweepeeIngredient.getModNameComponent(
            this.profession == null
                ? "minecraft"
                : BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.profession).getNamespace()));

        return tooltip;
    }

    @Override
    public Component getName() {
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
            var biomeLoc = BuiltInRegistries.VILLAGER_TYPE.getKey(this.biome);
            component.append(Component.translatable("biome." + biomeLoc.getNamespace() + "." + biomeLoc.getPath()));
            addedAny = true;
        }

        if (profession != null) {
            // We've for sure added something
            component.append(" ");
            var professionLoc = BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.profession);
            // TODO: what's the convention used for modded villager types?
            // Villager::getTypeName implies that it there's no namespace information.
            // i hope there is some convention
            component.append(Component.translatable("entity.minecraft.villager." + professionLoc.getPath()));
        } else {
            if (addedAny) {
                component.append(" ");
            }
            component.append(EntityType.VILLAGER.getDescription());
        }

        return component;
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
            buf.writeVarInt(BuiltInRegistries.VILLAGER_PROFESSION.getId(this.profession));
        } else {
            buf.writeVarInt(0);
        }
        if (this.biome != null) {
            buf.writeVarInt(1);
            buf.writeVarInt(BuiltInRegistries.VILLAGER_TYPE.getId(this.biome));
        } else {
            buf.writeVarInt(0);
        }
        buf.writeInt(this.minLevel);
    }

    public static VillagerIngredient deserialize(JsonObject json) {
        VillagerProfession profession = null;
        if (json.has("profession") && !json.get("profession").isJsonNull()) {
            profession = BuiltInRegistries.VILLAGER_PROFESSION.get(new ResourceLocation(GsonHelper.getAsString(json,
                "profession")));
        }
        VillagerType biome = null;
        if (json.has("biome") && !json.get("biome").isJsonNull()) {
            biome = BuiltInRegistries.VILLAGER_TYPE.get(new ResourceLocation(GsonHelper.getAsString(json, "biome")));
        }
        int minLevel = GsonHelper.getAsInt(json, "minLevel");
        return new VillagerIngredient(profession, biome, minLevel);
    }

    public static VillagerIngredient read(FriendlyByteBuf buf) {
        VillagerProfession profession = null;
        var hasProfession = buf.readVarInt();
        if (hasProfession != 0) {
            profession = BuiltInRegistries.VILLAGER_PROFESSION.byId(buf.readVarInt());
        }
        VillagerType biome = null;
        var hasBiome = buf.readVarInt();
        if (hasBiome != 0) {
            biome = BuiltInRegistries.VILLAGER_TYPE.byId(buf.readVarInt());
        }
        int minLevel = buf.readInt();
        return new VillagerIngredient(profession, biome, minLevel);
    }

    @Override
    public Type ingrType() {
        return Type.VILLAGER;
    }

    @Override
    public String getSomeKindOfReasonableIDForEmi() {
        var bob = new StringBuilder();
        if (this.profession != null) {
            var profLoc = BuiltInRegistries.VILLAGER_PROFESSION.getKey(this.profession);
            bob.append(profLoc.getNamespace())
                .append("//")
                .append(profLoc.getPath());
        } else {
            bob.append("null");
        }
        bob.append("_");
        if (this.biome != null) {
            var biomeLoc = BuiltInRegistries.VILLAGER_TYPE.getKey(this.biome);
            bob.append(biomeLoc.getNamespace())
                .append("//")
                .append(biomeLoc.getPath());
        } else {
            bob.append("null");
        }

        bob.append(this.minLevel);
        return bob.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VillagerIngredient that = (VillagerIngredient) o;
        return minLevel == that.minLevel && Objects.equals(profession, that.profession) && Objects.equals(biome,
            that.biome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profession, biome, minLevel);
    }
}
