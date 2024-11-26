package at.petrak.hexcasting.forge.mixin;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BuiltInRegistries.class)
public interface ForgeAccessorBuiltInRegistries {
    @Invoker("registerDefaulted")
    static <T> DefaultedRegistry<T> hex$registerDefaulted(ResourceKey<? extends Registry<T>> registryName,
                                                          String defaultId,
                                                          BuiltInRegistries.RegistryBootstrap<T> bootstrap) {
        throw new IllegalStateException();
    }

    @Invoker("registerSimple")
    static <T> Registry<T> hex$registerSimple(ResourceKey<? extends Registry<T>> registryName,
                                              BuiltInRegistries.RegistryBootstrap<T> bootstrap) {
        throw new IllegalStateException();
    }
}

