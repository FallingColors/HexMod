package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.common.misc.Brainsweeping;
import net.minecraft.world.entity.monster.Witch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Prevents the witch from drinking potions
@Mixin(Witch.class)
public class MixinWitch {
    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;isAlive()Z"))
    private boolean isAliveForAiPurposes(Witch instance) {
        var self = (Witch) (Object) this;
        return self.isAlive() && !Brainsweeping.isBrainswept(self);
    }
}
