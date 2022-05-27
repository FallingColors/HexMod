package at.petrak.hexcasting.fabric.mixin.client;

import at.petrak.hexcasting.client.particles.ConjureParticle;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ParticleEngine.class)
public class FabricParticleEngineMixin {
	@Mutable
	@Final
	@Shadow
	private static List<ParticleRenderType> RENDER_ORDER;

	@Inject(at = @At("RETURN"), method = "<clinit>")
	private static void addTypes(CallbackInfo ci) {
		RENDER_ORDER = ImmutableList.<ParticleRenderType>builder().addAll(RENDER_ORDER)
				.add(ConjureParticle.CONJURE_RENDER_TYPE, ConjureParticle.LIGHT_RENDER_TYPE)
				.build();
	}
}
