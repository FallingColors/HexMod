package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Entity.class, remap = false)
public interface AccessorEntity {
    @Invoker("markHurt")
    void hex$markHurt();
}
