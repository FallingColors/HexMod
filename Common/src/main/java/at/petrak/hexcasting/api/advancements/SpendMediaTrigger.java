package at.petrak.hexcasting.api.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SpendMediaTrigger extends SimpleCriterionTrigger<SpendMediaTrigger.Instance> {
    private static final String TAG_MEDIA_SPENT = "media_spent";
    private static final String TAG_MEDIA_WASTED = "media_wasted";

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, long mediaSpent, long mediaWasted) {
        super.trigger(player, inst -> inst.test(mediaSpent, mediaWasted));
    }

    public static record Instance(Optional<ContextAwarePredicate> player,
                                  MinMaxLongs mediaSpent,
                                  MinMaxLongs mediaWasted) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player),
            MinMaxLongs.CODEC.optionalFieldOf(TAG_MEDIA_SPENT, MinMaxLongs.ANY).forGetter(Instance::mediaSpent),
            MinMaxLongs.CODEC.optionalFieldOf(TAG_MEDIA_WASTED, MinMaxLongs.ANY).forGetter(Instance::mediaWasted)
        ).apply(inst, Instance::new));

        public Instance(ContextAwarePredicate player, MinMaxLongs mediaSpent, MinMaxLongs mediaWasted) {
            this(Optional.of(player), mediaSpent, mediaWasted);
        }

        private boolean test(long mediaSpentIn, long mediaWastedIn) {
            return this.mediaSpent.matches(mediaSpentIn) && this.mediaWasted.matches(mediaWastedIn);
        }
    }
}
