package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.JsonObject;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;

import java.util.List;

public abstract class BrainsweepIngredient {
    public abstract boolean test(Entity entity, ServerLevel level);

    public abstract List<Component> getTooltip(boolean advanced);

    public abstract JsonObject serialize();

    public abstract void write(FriendlyByteBuf buf);

    public abstract Entity exampleEntity(ClientLevel level);

    public static BrainsweepIngredient read(FriendlyByteBuf buf) {
        var type = buf.readVarInt();
        return switch (Type.values()[type]) {
            case VILLAGER -> VillagerBrainsweepIngredient.read(buf);
            case ENTITY -> EntityBrainsweepIngredient.read(buf);
        };
    }

    public static BrainsweepIngredient deserialize(JsonObject json) {
        var typestr = GsonHelper.getAsString(json, "type");
        var type = Type.valueOf(typestr);
        return switch (type) {
            case VILLAGER -> VillagerBrainsweepIngredient.deserialize(json);
            case ENTITY -> EntityBrainsweepIngredient.deserialize(json);
        };
    }

    public enum Type implements StringRepresentable {
        VILLAGER,
        ENTITY;

        @Override
        public String getSerializedName() {
            return this.toString();
        }
    }

}
