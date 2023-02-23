package at.petrak.hexcasting.common.particles;

import at.petrak.hexcasting.common.lib.HexParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

public record ConjureParticleOptions(int color) implements ParticleOptions {
    @Override
    public ParticleType<?> getType() {
        return HexParticles.CONJURE_PARTICLE;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeInt(this.color);
    }

    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %s", this.color);
    }

    public static final Deserializer<ConjureParticleOptions> DESERIALIZER = new Deserializer<>() {
        @Override
        public ConjureParticleOptions fromCommand(ParticleType<ConjureParticleOptions> type,
            StringReader reader) throws CommandSyntaxException {

            reader.expect(' ');
            var color = reader.readInt();
            return new ConjureParticleOptions(color);
        }

        @Override
        public ConjureParticleOptions fromNetwork(ParticleType<ConjureParticleOptions> type,
            FriendlyByteBuf buf) {
            var col = buf.readInt();
            return new ConjureParticleOptions(col);
        }
    };

    public static class Type extends ParticleType<ConjureParticleOptions> {
        public Type(boolean pOverrideLimiter) {
            super(pOverrideLimiter, DESERIALIZER);
        }

        public static final Codec<ConjureParticleOptions> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.INT.fieldOf("color")
                        .forGetter((ConjureParticleOptions o) -> o.color)
                )
                .apply(instance, ConjureParticleOptions::new)
        );

        @Override
        public Codec<ConjureParticleOptions> codec() {
            return CODEC;
        }
    }
}
