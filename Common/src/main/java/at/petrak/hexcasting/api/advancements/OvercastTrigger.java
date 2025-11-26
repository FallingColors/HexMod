package at.petrak.hexcasting.api.advancements;

import at.petrak.hexcasting.api.mod.HexConfig;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

// https://github.com/TelepathicGrunt/Bumblezone/blob/latest-released/src/main/java/com/telepathicgrunt/the_bumblezone/advancements/CleanupStickyHoneyResidueTrigger.java
// https://github.com/VazkiiMods/Botania/blob/b8706e2e0bba20f67f1e103559a4ce39d63d48f9/src/main/java/vazkii/botania/common/advancements/CorporeaRequestTrigger.java

public class OvercastTrigger extends SimpleCriterionTrigger<OvercastTrigger.Instance> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("hexcasting", "overcast");

    private static final String TAG_MEDIA_GENERATED = "media_generated";
    private static final String TAG_HEALTH_USED = "health_used";
    // HEY KIDS DID YOYU KNOW THERE'S NOT A CRITERIA FOR HOW MUCH ***HEALTH*** AN ENTITY HAS
    private static final String TAG_HEALTH_LEFT =
        "mojang_i_am_begging_and_crying_please_add_an_entity_health_criterion";

    public void trigger(ServerPlayer player, int mediaGenerated) {
        super.trigger(player, inst -> {
            var mediaToHealth = HexConfig.common().mediaToHealthRate();
            var healthUsed = mediaGenerated / mediaToHealth;
            return inst.test(mediaGenerated, healthUsed / player.getMaxHealth(), player.getHealth());
        });
    }

    @Override
    public Codec<Instance> codec() {
        return null;
    }

    public record Instance(MinMaxBounds.Ints mediaGenerated, MinMaxBounds.Doubles healthUsed, MinMaxBounds.Doubles healthLeft, Optional<ContextAwarePredicate> predicate) implements SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create((instance) -> (instance.group(
                MinMaxBounds.Ints.CODEC.fieldOf("mediaGenerated").forGetter(Instance::mediaGenerated),
                MinMaxBounds.Doubles.CODEC.fieldOf("healthUsed").forGetter(Instance::healthUsed),
                MinMaxBounds.Doubles.CODEC.fieldOf("healthLeft").forGetter(Instance::healthLeft),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)
        )).apply(instance, Instance::new));

        private boolean test(int mediaGeneratedIn, double healthUsedIn, float healthLeftIn) {
            return this.mediaGenerated.matches(mediaGeneratedIn)
                && this.healthUsed.matches(healthUsedIn)
                // DID YOU KNOW ALL THE ENEITYT PREDICATES ARE HARD-CODED AND YOU CANT MAKE NEW ONES
                && this.healthLeft.matches(healthLeftIn);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return null;
        }
    }
}
