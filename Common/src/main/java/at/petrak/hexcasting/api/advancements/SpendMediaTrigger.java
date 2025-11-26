package at.petrak.hexcasting.api.advancements;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SpendMediaTrigger extends SimpleCriterionTrigger<SpendMediaTrigger.Instance> {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("hexcasting", "spend_media");

    private static final String TAG_MEDIA_SPENT = "media_spent";
    private static final String TAG_MEDIA_WASTED = "media_wasted";

    public void trigger(ServerPlayer player, long mediaSpent, long mediaWasted) {
        super.trigger(player, inst -> inst.test(mediaSpent, mediaWasted));
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public static record Instance(MinMaxLongs mediaSpent, MinMaxLongs mediaWasted) implements SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                MinMaxLongs.CODEC.fieldOf("mediaSpent").forGetter(Instance::mediaSpent),
                MinMaxLongs.CODEC.fieldOf("mediaWasted").forGetter(Instance::mediaWasted)
        ).apply(instance, Instance::new));

        private boolean test(long mediaSpentIn, long mediaWastedIn) {
            return this.mediaSpent.matches(mediaSpentIn) && this.mediaWasted.matches(mediaWastedIn);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }
    }
}
