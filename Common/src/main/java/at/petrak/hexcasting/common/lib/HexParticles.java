package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.client.particles.ConjureParticle;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;

import java.util.function.Function;
import java.util.function.Supplier;

public class HexParticles {
    private static final IXplatRegister<ParticleType<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(Registries.PARTICLE_TYPE);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Supplier<ConjureParticleOptions.Type> CONJURE_PARTICLE = REGISTER.register(
        "conjure_particle", () -> new ConjureParticleOptions.Type(false));

    public static class FactoryHandler {
        public interface Consumer {
            <T extends ParticleOptions> void register(ParticleType<T> type,
                Function<SpriteSet, ParticleProvider<T>> constructor);
        }

        public static void registerFactories(Consumer consumer) {
            consumer.register(CONJURE_PARTICLE.get(), ConjureParticle.Provider::new);
        }
    }
}
