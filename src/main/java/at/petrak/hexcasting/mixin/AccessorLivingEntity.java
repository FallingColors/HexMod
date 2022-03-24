package at.petrak.hexcasting.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface AccessorLivingEntity {
    @Accessor("brain")
    void hex$SetBrain(Brain<?> brain);
}
