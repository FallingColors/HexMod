package at.petrak.hexcasting.api.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SpendManaTrigger extends SimpleCriterionTrigger<SpendManaTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("hexcasting", "spend_mana");

    private static final String TAG_MANA_SPENT = "mana_spent";
    private static final String TAG_MANA_WASTED = "mana_wasted";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Instance createInstance(JsonObject json, EntityPredicate.Composite predicate,
        DeserializationContext pContext) {
        return new Instance(predicate,
            MinMaxBounds.Ints.fromJson(json.get(TAG_MANA_SPENT)),
            MinMaxBounds.Ints.fromJson(json.get(TAG_MANA_WASTED)));
    }

    public void trigger(ServerPlayer player, int manaSpent, int manaWasted) {
        super.trigger(player, inst -> inst.test(manaSpent, manaWasted));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        protected final MinMaxBounds.Ints manaSpent;
        protected final MinMaxBounds.Ints manaWasted;

        public Instance(EntityPredicate.Composite predicate, MinMaxBounds.Ints manaSpent,
            MinMaxBounds.Ints manaWasted) {
            super(SpendManaTrigger.ID, predicate);
            this.manaSpent = manaSpent;
            this.manaWasted = manaWasted;
        }

        @Override
        public ResourceLocation getCriterion() {
            return ID;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext ctx) {
            JsonObject json = super.serializeToJson(ctx);
            if (!this.manaSpent.isAny()) {
                json.add(TAG_MANA_SPENT, this.manaSpent.serializeToJson());
            }
            if (!this.manaWasted.isAny()) {
                json.add(TAG_MANA_WASTED, this.manaWasted.serializeToJson());
            }
            return json;
        }

        private boolean test(int manaSpentIn, int manaWastedIn) {
            return this.manaSpent.matches(manaSpentIn) && this.manaWasted.matches(manaWastedIn);
        }
    }
}
