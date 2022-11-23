package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.List;

// Code based on:
// https://github.com/SlimeKnights/Mantle/blob/1.18.2/src/main/java/slimeknights/mantle/recipe/ingredient/EntityIngredient.java
// Licensed under MIT
public class EntityBrainsweepIngredient extends BrainsweepIngredient {
    public static final Gson GSON = new GsonBuilder().create();

    public final EntityPredicate requirements;
    // Just tell the player what it is you want
    public final Component tooltip;

    public EntityBrainsweepIngredient(EntityPredicate requirements, Component tooltip) {
        this.requirements = requirements;
        this.tooltip = tooltip;
    }

    @Override
    public boolean test(Entity entity, ServerLevel level) {
        return this.requirements.matches(level, null, entity);
    }

    @Override
    public List<Component> getTooltip(boolean advanced) {
        return List.of(this.tooltip);
    }

    @Override
    public JsonObject serialize() {
        var obj = new JsonObject();
        obj.addProperty("type", "entity");

        obj.add("requirements", this.requirements.serializeToJson());
        obj.addProperty("tooltip", Component.Serializer.toJson(this.tooltip));
        return obj;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(Type.ENTITY.ordinal());

        buf.writeUtf(this.requirements.serializeToJson().toString());
        buf.writeUtf(Component.Serializer.toJson(this.tooltip));
    }

    public static EntityBrainsweepIngredient deserialize(JsonObject obj) {
        var reqs = EntityPredicate.fromJson(obj.get("requirements"));
        var tooltip = Component.Serializer.fromJson(obj.get("tooltip"));
        return new EntityBrainsweepIngredient(reqs, tooltip);
    }

    public static EntityBrainsweepIngredient read(FriendlyByteBuf buf) {
        var reqsObj = GSON.fromJson(buf.readUtf(), JsonElement.class);

        var reqs = EntityPredicate.fromJson(reqsObj);
        var tooltip = Component.Serializer.fromJson(buf.readUtf());
        return new EntityBrainsweepIngredient(reqs, tooltip);
    }
}
