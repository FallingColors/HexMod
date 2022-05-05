package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(
        ForgeRegistries.PARTICLE_TYPES, HexMod.MOD_ID);

    public static final RegistryObject<ConjureParticleOptions.Type> CONJURE_PARTICLE = PARTICLES.register(
        "conjure_block_particle", () -> new ConjureParticleOptions.Type(false));
    public static final RegistryObject<ConjureParticleOptions.Type> LIGHT_PARTICLE = PARTICLES.register(
        "conjure_light_particle", () -> new ConjureParticleOptions.Type(false));
}
