package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.forge.ForgeHexInitializer;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ForgeRegister<B> implements IXplatRegister<B> {
    private final DeferredRegister<B> register;

    public ForgeRegister(DeferredRegister<B> register) {
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
