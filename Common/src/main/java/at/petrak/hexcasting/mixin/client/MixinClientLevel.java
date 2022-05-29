package at.petrak.hexcasting.mixin.client;

import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {

	@Inject(method = "doAnimateTick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;animateTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/Random;)V"),
			locals = LocalCapture.CAPTURE_FAILSOFT)
	public void addBuddingAmethystParticles(int baseX, int baseY, int baseZ, int range, Random rand, Block marked, BlockPos.MutableBlockPos pos, CallbackInfo ci,
											int trueX, int trueY, int trueZ, BlockState state) {
		ClientLevel self = ((ClientLevel) (Object) this);

		if (state.is(Blocks.BUDDING_AMETHYST)) {
			ParticleOptions options = new ConjureParticleOptions(0x8932b8, true);
			Vec3 center = Vec3.atCenterOf(pos);
			for (Direction direction : Direction.values()) {
				int dX = direction.getStepX();
				int dY = direction.getStepY();
				int dZ = direction.getStepZ();

				int count = rand.nextInt(10) / 5;
				for (int i = 0; i < count; i++) {
					double pX = center.x + (dX == 0 ? Mth.nextDouble(rand, -0.5D, 0.5D) : (double) dX * 0.55D);
					double pY = center.y + (dY == 0 ? Mth.nextDouble(rand, -0.5D, 0.5D) : (double) dY * 0.55D);
					double pZ = center.z + (dZ == 0 ? Mth.nextDouble(rand, -0.5D, 0.5D) : (double) dZ * 0.55D);
					self.addParticle(options, pX, pY, pZ, 0, 0, 0);
				}
			}
		}
	}

}
