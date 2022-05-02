package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import net.minecraft.world.entity.raid.Raider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Prevents the witch from joining a raid
@Mixin(Raider.class)
public class MixinRaider {
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/raid/Raider;isAlive()Z"))
    private boolean isAliveForAiPurposes(Raider instance) {
        var self = (Raider) (Object) this;
        return self.isAlive() && !Brainsweeping.isBrainswept(self);
    }
}
