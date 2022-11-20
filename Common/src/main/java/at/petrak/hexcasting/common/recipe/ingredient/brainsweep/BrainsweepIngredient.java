package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.function.Predicate;

public abstract class BrainsweepIngredient implements Predicate<Entity> {
    public abstract List<Component> getTooltip(boolean advanced);

    public abstract JsonObject serialize();

    public abstract void write(FriendlyByteBuf buf);

    public static BrainsweepIngredient read(FriendlyByteBuf buf) {
        var type = buf.readVarInt();
        return switch (Type.values()[type]) {
            case VILLAGER -> VillagerBrainsweepIngredient.read(buf);
            case ENTITY -> {
            }
        };
    }

    public static BrainsweepIngredient deserialize(JsonObject json) {
        var typestr = GsonHelper.getAsString(json, "type");
        var type = Type.valueOf(typestr);
        return switch (type) {
            case VILLAGER -> VillagerBrainsweepIngredient.deserialize(json);
            case ENTITY -> null;
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
