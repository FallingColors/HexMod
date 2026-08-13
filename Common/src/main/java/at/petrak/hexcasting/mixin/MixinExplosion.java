package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.helper.ExplosionSourceTracker;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Explosion.class)
public abstract class MixinExplosion {
    @ModifyArgs(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private void scaleEntityExplosionDamage(Args args) {
        if (ExplosionSourceTracker.isSpellSource()) {
            args.set(1, (Float) args.get(1) * HexConfig.common().explosionScaling() / 100f);
        }
    }
}
