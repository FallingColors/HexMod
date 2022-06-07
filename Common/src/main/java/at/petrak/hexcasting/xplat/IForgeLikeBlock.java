package at.petrak.hexcasting.xplat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An interface that mimics some methods of IForgeBlock.
 * Fabric implementation will use mixins to achieve the same effects.
 */
public interface IForgeLikeBlock {
	default boolean addLandingEffects(BlockState state, ServerLevel level, BlockPos pos, LivingEntity entity, int numberOfParticles) {
		return false;
	}

	default boolean hasEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
		return false;
	}
}
