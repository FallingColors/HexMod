package at.petrak.hexcasting.common.particles;

import at.petrak.hexcasting.HexMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HexParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, HexMod.MOD_ID);

    public static final RegistryObject<SimpleParticleType> CONJURE_LIGHT_PARTICLE = PARTICLES.register("conjure_light_particle", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> CONJURE_BLOCK_PARTICLE = PARTICLES.register("conjure_block_particle", () -> new SimpleParticleType(true));
}
