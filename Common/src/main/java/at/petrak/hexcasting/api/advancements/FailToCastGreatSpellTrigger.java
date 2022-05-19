package at.petrak.hexcasting.api.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class FailToCastGreatSpellTrigger extends SimpleCriterionTrigger<FailToCastGreatSpellTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("hexcasting", "fail_to_cast_great_spell");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, EntityPredicate.Composite predicate,
        DeserializationContext pContext) {
        return new Instance(predicate);
    }

    public void trigger(ServerPlayer player) {
        super.trigger(player, e -> true);
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        public Instance(EntityPredicate.Composite pPlayer) {
            super(ID, pPlayer);
        }

        @Override
        public ResourceLocation getCriterion() {
            return ID;
        }

        public JsonObject serializeToJson(SerializationContext pConditions) {
            return new JsonObject();
        }
    }
}
