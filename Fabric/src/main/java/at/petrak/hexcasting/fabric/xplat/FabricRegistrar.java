package at.petrak.hexcasting.fabric.xplat;

import at.petrak.hexcasting.xplat.IXplatRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class FabricRegistrar<B> implements IXplatRegistrar<B> {
    private final Registry<B> registry;

    @SuppressWarnings("unchecked")
    public FabricRegistrar(ResourceKey<Registry<B>> registryKey) {
        this.registry = (Registry<B>) BuiltInRegistries.REGISTRY.get(registryKey.location());
    }


    @Override
    public <T extends B> Supplier<T> register(String id, Supplier<T> provider) {
        T value = provider.get();
        Registry.register(registry, modLoc(id), value);
        return () -> value;
    }

    @Override
    public <T extends B> Holder<B> registerHolder(String id, Supplier<T> provider) {
        T value = provider.get();
        Registry.register(registry, modLoc(id), value);
        return registry.wrapAsHolder(value);
    }

    @Override
    public void registerAll() {
        // This is fabric. we register eagerly.
    }
}
