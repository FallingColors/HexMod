package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.xplat.IForgeLikeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class FabricLivingEntityMixin {
    @Unique
    private BlockState hex$cachedParticleState;

    @ModifyVariable(method = "checkFallDamage", at = @At(value = "LOAD", ordinal = 0), argsOnly = true)
    private BlockState overwrite(BlockState state, double d, boolean bl, BlockState _ignored, BlockPos pos) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (state.getBlock() instanceof IForgeLikeBlock forgeLike) {
            float dist = (float) Mth.ceil(entity.fallDistance - 3.0F);
            double e = Math.min(0.2F + dist / 15.0F, 2.5D);
            int i = (int)(150.0D * e);
            if (forgeLike.addLandingEffects(state, (ServerLevel) entity.level, pos, entity, i)) {
                hex$cachedParticleState = state;
                return Blocks.AIR.defaultBlockState();
            }
        }

        return state;
    }

    @ModifyArg(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"))
    private BlockState restore(BlockState state) {
        if (hex$cachedParticleState != null) {
            BlockState cached = hex$cachedParticleState;
            hex$cachedParticleState = null;
            return cached;
        }
        return state;
    }
}
