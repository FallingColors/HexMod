package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.forge.ForgeHexInitializer;
import at.petrak.hexcasting.xplat.IXplatRegistrar;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ForgeRegistrar<B> implements IXplatRegistrar<B> {
    private final DeferredRegister<B> register;

    public ForgeRegistrar(DeferredRegister<B> register) {
        this.register = register;
    }

    @Override
    public void registerAll() {
        register.register(ForgeHexInitializer.getModEventBus());
    }

    @Override
    public <T extends B> Supplier<T> register(String id, Supplier<T> provider) {
        return register.register(id, provider);
    }

    @Override
    public <T extends B> Holder<B> registerHolder(String id, Supplier<T> provider) {
        return register.register(id, provider);
    }
}
