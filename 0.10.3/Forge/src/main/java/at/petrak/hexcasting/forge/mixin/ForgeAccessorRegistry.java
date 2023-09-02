package at.petrak.hexcasting.forge.mixin;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Registry.class)
public interface ForgeAccessorRegistry {
    @Invoker("registerDefaulted")
    static <T> DefaultedRegistry<T> hex$registerDefaulted(ResourceKey<? extends Registry<T>> registryName,
        String defaultId,
        Registry.RegistryBootstrap<T> bootstrap) {
        throw new IllegalStateException();
    }
}
