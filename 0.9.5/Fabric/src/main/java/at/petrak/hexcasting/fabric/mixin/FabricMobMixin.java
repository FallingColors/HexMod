package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.fabric.event.VillagerConversionCallback;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class FabricMobMixin {
    @Inject(method = "convertTo", at = @At("RETURN"))
    public <T extends Mob> void onThunderHit(EntityType<T> entityType, boolean bl, CallbackInfoReturnable<T> cir) {
        var self = (Mob) (Object) this;
        var mob = cir.getReturnValue();
        if (mob != null) {
            VillagerConversionCallback.EVENT.invoker().interact(self, mob);
        }
    }
}
