package at.petrak.hexcasting.mixin;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.common.lib.HexMobEffects;
import at.petrak.hexcasting.common.misc.HexMobEffect;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Inject(method = "dropAllDeathLoot", require = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V"))
    private void dropCrystallizedLoot(CallbackInfo ci) {
        var self = (LivingEntity) (Object) this;
        var rand = self.getRandom();
        if (self.hasEffect(HexMobEffects.CRYSTALLIZED)) {
            int fibers = rand.nextIntBetweenInclusive(1,3);
            int dust = rand.nextIntBetweenInclusive(4,7);
            int extra = 0;
            if (self instanceof Villager vill) {
                int scaledLvl = vill.getVillagerData().getLevel() - 2;
                if (rand.nextIntBetweenInclusive(0, 1) <= scaledLvl) extra++;
                if (rand.nextIntBetweenInclusive(2, 3) <= scaledLvl) extra++;
            }
            self.spawnAtLocation(new ItemStack(HexItems.NEURAL_FIBER.get(), fibers + extra));
            self.spawnAtLocation(new ItemStack(HexItems.AMETHYST_DUST.get(), dust + extra));
        }
    }
}
