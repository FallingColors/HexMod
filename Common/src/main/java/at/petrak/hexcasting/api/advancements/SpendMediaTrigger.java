package at.petrak.hexcasting.api.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SpendMediaTrigger extends SimpleCriterionTrigger<SpendMediaTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("hexcasting", "spend_media");

    private static final String TAG_MEDIA_SPENT = "media_spent";
    private static final String TAG_MEDIA_WASTED = "media_wasted";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate predicate,
        DeserializationContext context) {
        return new Instance(predicate,
            MinMaxLongs.fromJson(json.get(TAG_MEDIA_SPENT)),
            MinMaxLongs.fromJson(json.get(TAG_MEDIA_WASTED)));
    }

    public void trigger(ServerPlayer player, long mediaSpent, long mediaWasted) {
        super.trigger(player, inst -> inst.test(mediaSpent, mediaWasted));
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        protected final MinMaxLongs mediaSpent;
        protected final MinMaxLongs mediaWasted;

        public Instance(ContextAwarePredicate predicate, MinMaxLongs mediaSpent,
            MinMaxLongs mediaWasted) {
            super(SpendMediaTrigger.ID, predicate);
            this.mediaSpent = mediaSpent;
            this.mediaWasted = mediaWasted;
        }

        @Override
        public ResourceLocation getCriterion() {
            return ID;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext ctx) {
            JsonObject json = super.serializeToJson(ctx);
            if (!this.mediaSpent.isAny()) {
                json.add(TAG_MEDIA_SPENT, this.mediaSpent.serializeToJson());
            }
            if (!this.mediaWasted.isAny()) {
                json.add(TAG_MEDIA_WASTED, this.mediaWasted.serializeToJson());
            }
            return json;
        }

        private boolean test(long mediaSpentIn, long mediaWastedIn) {
            return this.mediaSpent.matches(mediaSpentIn) && this.mediaWasted.matches(mediaWastedIn);
        }
    }
}
