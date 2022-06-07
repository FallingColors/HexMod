package at.petrak.hexcasting.xplat;

import at.petrak.hexcasting.annotations.SoftImplement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An interface that mimics some methods of IForgeBlock.
 * Fabric implementation will use mixins to achieve the same effect.
 */
public interface IForgeLikeBlock {
	@SoftImplement("forge")
	default boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return false;
	}

	/**
	 * On Fabric, this method's return value doesn't matter - it's only checked for whether it's greater than 0.
	 * Implementing boosts the way Forge does would... not be worth the effort.
	 */
	@SoftImplement("forge")
	default float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
		return 0;
	}
}
