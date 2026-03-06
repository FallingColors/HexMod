package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractArrow.class, remap = false)
public interface AccessorAbstractArrow {
    @Accessor("inGround")
    boolean hex$isInGround();
}
