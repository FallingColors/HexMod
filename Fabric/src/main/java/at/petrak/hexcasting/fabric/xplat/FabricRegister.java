package at.petrak.hexcasting.fabric.xplat;

import at.petrak.hexcasting.xplat.IXplatRegister;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class FabricRegister<B> implements IXplatRegister<B> {
    private final MappedRegistry<B> register;
    private final Map<String, B> map;

    public FabricRegister(MappedRegistry<B> register) {
        this.register = register;
        this.map = new HashMap<>();
    }

    public FabricRegister(MappedRegistry<B> register, Map<String, B> map) {
        this.register = register;
        this.map = map;
    }

    @Override
    public <T extends B> Supplier<T> register(String id, Supplier<T> provider) {
        var key = ResourceKey.create(
                register.key(),
                modLoc(id)
        );
        var value = provider.get();
        var info = RegistrationInfo.BUILT_IN;
        register.register(key, value, info);
        return provider;
    }

    @Override
    public <T extends B> Holder<B> registerHolder(String id, Supplier<T> provider) {
        map.put(id, provider.get());
        return register.wrapAsHolder(provider.get());
    }

    @Override
    public void registerAll() {
        map.forEach((string, provider) ->
                register.register(
                        ResourceKey.create(
                                register.key(), modLoc(string)
                        ), provider, RegistrationInfo.BUILT_IN)
                );
    }
}
