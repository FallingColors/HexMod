package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface AccessorLivingEntity {
    @Accessor("lastHurt")
    float hex$getLastHurt();

    @Accessor("lastHurt")
    void hex$setLastHurt(float lastHurt);
}
