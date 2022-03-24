package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Prevents the villager from any of its brain goals or making ambient noise
@Mixin(Villager.class)
public class MixinVillager {
    @Inject(method = "registerBrainGoals", at = @At("HEAD"), cancellable = true)
    private void onRegisterBrainGoals(CallbackInfo ci) {
        var self = (Villager) (Object) this;
        if (Brainsweeping.isBrainswept(self)) {
            ci.cancel();
        }
    }

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true)
    protected void onGetAmbientSound(CallbackInfoReturnable<SoundEvent> ci) {
        var self = (Villager) (Object) this;
        if (Brainsweeping.isBrainswept(self)) {
            ci.setReturnValue(null);
        }
    }
}
