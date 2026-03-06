package at.petrak.hexcasting.api.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class FailToCastGreatSpellTrigger extends SimpleCriterionTrigger<FailToCastGreatSpellTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        super.trigger(player, e -> true);
    }

    public static record Instance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player)
        ).apply(inst, Instance::new));

        public Instance(ContextAwarePredicate player) {
            this(Optional.of(player));
        }
    }
}
