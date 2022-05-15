package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.fabric.event.VillagerConversionCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Villager.class)
public class FabricVillagerTurnIntoWitchMixin {
    @Inject(method = "thunderHit", locals = LocalCapture.CAPTURE_FAILSOFT, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"))
    public void onThunderHit(ServerLevel serverLevel, LightningBolt lightningBolt, CallbackInfo ci, Witch newWitch) {
        var self = (Villager) (Object) this;
        VillagerConversionCallback.EVENT.invoker().interact(self, newWitch);
    }
}
