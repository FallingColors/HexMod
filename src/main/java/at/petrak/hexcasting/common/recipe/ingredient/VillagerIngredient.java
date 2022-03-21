package at.petrak.hexcasting.common.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.npc.Villager;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

// you ever step back and realize the thoughts that have coursed through your mind for so long
// they've become second nature are in fact incredibly horrific?
// jesus christ I'm making a class called `VillagerIngredient`
public record VillagerIngredient(
    @Nullable ResourceLocation profession,
    @Nullable ResourceLocation biome,     // aka their "type"
    int minLevel
) implements Predicate<Villager> {
    @Override
    public boolean test(Villager villager) {
        var data = villager.getVillagerData();

        return (this.profession == null || this.profession.equals(data.getProfession().getRegistryName()))
            && (this.biome == null || this.biome.equals(Registry.VILLAGER_TYPE.getKey(data.getType())))
            && this.minLevel <= data.getLevel();
    }

    public JsonObject serialize() {
        var obj = new JsonObject();
        if (this.profession != null) {
            obj.addProperty("profession", this.profession.toString());
        }
        if (this.biome != null) {
            obj.addProperty("biome", this.biome.toString());
        }
        obj.addProperty("minLevel", this.minLevel);
        return obj;
    }

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
        if (json.has("profession")) {
            profession = new ResourceLocation(GsonHelper.getAsString(json, "profession"));
        }
        ResourceLocation biome = null;
        if (json.has("biome")) {
            biome = new ResourceLocation(GsonHelper.getAsString(json, "biome"));
        }
        int minLevel = GsonHelper.getAsInt(json, "minLevel");
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
