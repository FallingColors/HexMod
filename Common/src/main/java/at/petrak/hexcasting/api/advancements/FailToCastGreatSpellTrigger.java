package at.petrak.hexcasting.api.advancements;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.wispforest.endec.SerializationContext;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class FailToCastGreatSpellTrigger extends SimpleCriterionTrigger<FailToCastGreatSpellTrigger.Instance> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("hexcasting", "fail_to_cast_great_spell");

    public void trigger(ServerPlayer player) {
        super.trigger(player, e -> true);
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public static record Instance(Optional<ContextAwarePredicate> predicate) implements SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create((instance) -> (instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("predicate").forGetter(Instance::predicate)
        )).apply(instance, Instance::new));

        public JsonObject serializeToJson(SerializationContext pConditions) {
            return new JsonObject();
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }
    }
}
