package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface AccessorLivingEntity {
    @Accessor("lastHurt")
    float hex$getLastHurt();

    @Accessor("lastHurt")
    void hex$setLastHurt(float lastHurt);

    @Invoker("playHurtSound")
    void hex$playHurtSound(DamageSource source);

    @Invoker("checkTotemDeathProtection")
    boolean hex$checkTotemDeathProtection(DamageSource source);

    @Invoker("getDeathSound")
    SoundEvent hex$getDeathSound();

    @Invoker("getSoundVolume")
    float hex$getSoundVolume();

    @Accessor("lastDamageSource")
    void hex$setLastDamageSource(DamageSource source);

    @Accessor("lastDamageStamp")
    void hex$setLastDamageStamp(long stamp);
}
