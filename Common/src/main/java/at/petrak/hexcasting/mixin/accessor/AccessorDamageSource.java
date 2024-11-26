package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DamageSources.class)
public interface AccessorDamageSource {
    @Invoker("source")
    @Nullable
    DamageSource hex$source(ResourceKey<DamageType> key, @Nullable Entity entity);
}
