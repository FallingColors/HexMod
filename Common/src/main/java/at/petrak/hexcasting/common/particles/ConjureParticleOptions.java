package at.petrak.hexcasting.common.particles;

import at.petrak.hexcasting.common.lib.HexParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;

public record ConjureParticleOptions(int color) implements ParticleOptions {
    @Override
    public ParticleType<?> getType() {
        return HexParticles.CONJURE_PARTICLE;
    }

    public static class Type extends ParticleType<ConjureParticleOptions> {
        public Type(boolean pOverrideLimiter) {
            super(pOverrideLimiter);
        }

        public static final MapCodec<ConjureParticleOptions> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.INT.fieldOf("color")
                        .forGetter((ConjureParticleOptions o) -> o.color)
                )
                .apply(instance, ConjureParticleOptions::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ConjureParticleOptions> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, ConjureParticleOptions::color,
                        ConjureParticleOptions::new
                );

        @Override
        public MapCodec<ConjureParticleOptions> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ConjureParticleOptions> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
