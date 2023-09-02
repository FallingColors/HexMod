package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Prevents brainswept mobs from having an AI tick
@Mixin(Mob.class)
public class MixinMob {
    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void onRegisterBrainGoals(CallbackInfo ci) {
        var self = (Mob) (Object) this;
        if (Brainsweeping.isBrainswept(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "playAmbientSound", at = @At("HEAD"), cancellable = true)
    protected void onPlayAmbientSound(CallbackInfo ci) {
        var self = (Mob) (Object) this;
        if (Brainsweeping.isBrainswept(self)) {
            ci.cancel();
        }
    }
}
