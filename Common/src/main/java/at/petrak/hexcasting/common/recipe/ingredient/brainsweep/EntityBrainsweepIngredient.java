package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class EntityBrainsweepIngredient extends BrainsweepIngredient {
    public final EntityPredicate.Composite requirements;
    // Just tell the player what it is you want
    public final Component tooltip;

    protected EntityBrainsweepIngredient(EntityPredicate.Composite requirements, Component tooltip) {
        super();
        this.requirements = requirements;
        this.tooltip = tooltip;
    }

    @Override
    public boolean test(Entity entity) {
        return false;
    }

    @Override
    public List<Component> getTooltip(boolean advanced) {
        return List.of(this.tooltip);
    }

    @Override
    public JsonObject serialize() {
        var obj = new JsonObject();
        obj.add("requirements", this.requirements.toJson(SerializationContext.INSTANCE));
        obj.addProperty("tooltip", Component.Serializer.toJson(this.tooltip));
        return obj;
    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    public static EntityBrainsweepIngredient deserialize(JsonObject obj) {
        var reqs = EntityPredicate.Composite.fromJson(obj, "requirements", DeserializationContext)
    }
}
