package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Prevents the villager from any of its brain goals
@Mixin(Villager.class)
public class MixinVillager {
    @Inject(method = "registerBrainGoals", at = @At("HEAD"), cancellable = true)
    private void onRegisterBrainGoals(CallbackInfo ci) {
        var self = (Villager) (Object) this;
        if (Brainsweeping.isBrainswept(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "canBreed", at = @At("HEAD"), cancellable = true)
    private void preventBreeding(CallbackInfoReturnable<Boolean> cir) {
        var self = (Villager) (Object) this;
        if (Brainsweeping.isBrainswept(self)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setUnhappy", at = @At("HEAD"), cancellable = true)
    private void preventUnhappiness(CallbackInfo ci) {
        var self = (Villager) (Object) this;
        if (Brainsweeping.isBrainswept(self)) {
            ci.cancel();
        }
    }
}
