package at.petrak.hexcasting.api.advancements;

import at.petrak.hexcasting.api.mod.HexConfig;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

// https://github.com/TelepathicGrunt/Bumblezone/blob/latest-released/src/main/java/com/telepathicgrunt/the_bumblezone/advancements/CleanupStickyHoneyResidueTrigger.java
// https://github.com/VazkiiMods/Botania/blob/b8706e2e0bba20f67f1e103559a4ce39d63d48f9/src/main/java/vazkii/botania/common/advancements/CorporeaRequestTrigger.java

public class OvercastTrigger extends SimpleCriterionTrigger<OvercastTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation("hexcasting", "overcast");

    private static final String TAG_MEDIA_GENERATED = "media_generated";
    private static final String TAG_HEALTH_USED = "health_used";
    // HEY KIDS DID YOYU KNOW THERE'S NOT A CRITERIA FOR HOW MUCH ***HEALTH*** AN ENTITY HAS
    private static final String TAG_HEALTH_LEFT =
        "mojang_i_am_begging_and_crying_please_add_an_entity_health_criterion";

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate predicate,
        DeserializationContext pContext) {
        return new Instance(predicate,
            MinMaxBounds.Ints.fromJson(json.get(TAG_MEDIA_GENERATED)),
            MinMaxBounds.Doubles.fromJson(json.get(TAG_HEALTH_USED)),
            MinMaxBounds.Doubles.fromJson(json.get(TAG_HEALTH_LEFT)));
    }

    public void trigger(ServerPlayer player, int mediaGenerated) {
        super.trigger(player, inst -> {
            var mediaToHealth = HexConfig.common().mediaToHealthRate();
            var healthUsed = mediaGenerated / mediaToHealth;
            return inst.test(mediaGenerated, healthUsed / player.getMaxHealth(), player.getHealth());
        });
    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        protected final MinMaxBounds.Ints mediaGenerated;
        // This is the *proporttion* of the health bar.
        protected final MinMaxBounds.Doubles healthUsed;
        // DID YOU KNOW THERES ONE TO CHECK THE WORLD TIME, BUT NOT THE HEALTH!?
        protected final MinMaxBounds.Doubles healthLeft;

        public Instance(ContextAwarePredicate predicate, MinMaxBounds.Ints mediaGenerated,
            MinMaxBounds.Doubles healthUsed, MinMaxBounds.Doubles healthLeft) {
            super(OvercastTrigger.ID, predicate);
            this.mediaGenerated = mediaGenerated;
            this.healthUsed = healthUsed;
            // DID YOU KNOW THERE'S ONE TO CHECK THE FUCKING C A T T Y P E BUT NOT THE HEALTH
            this.healthLeft = healthLeft;
        }

        @Override
        public ResourceLocation getCriterion() {
            return ID;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext ctx) {
            JsonObject json = super.serializeToJson(ctx);
            if (!this.mediaGenerated.isAny()) {
                json.add(TAG_MEDIA_GENERATED, this.mediaGenerated.serializeToJson());
            }
            if (!this.healthUsed.isAny()) {
                json.add(TAG_HEALTH_USED, this.healthUsed.serializeToJson());
            }
            if (!this.healthLeft.isAny()) {
                json.add(TAG_HEALTH_LEFT, this.healthLeft.serializeToJson());
            }
            return json;
        }

        private boolean test(int mediaGeneratedIn, double healthUsedIn, float healthLeftIn) {
            return this.mediaGenerated.matches(mediaGeneratedIn)
                && this.healthUsed.matches(healthUsedIn)
                // DID YOU KNOW ALL THE ENEITYT PREDICATES ARE HARD-CODED AND YOU CANT MAKE NEW ONES
                && this.healthLeft.matches(healthLeftIn);
        }
    }
}
