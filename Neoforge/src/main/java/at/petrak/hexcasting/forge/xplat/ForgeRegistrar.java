package at.petrak.hexcasting.forge.xplat;

import at.petrak.hexcasting.forge.ForgeHexInitializer;
import at.petrak.hexcasting.xplat.IXplatRegistrar;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ForgeRegistrar<B> implements IXplatRegistrar<B> {
    private final DeferredRegister<B> deferredReg;

    public ForgeRegistrar(DeferredRegister<B> deferredReg) {
        this.deferredReg = deferredReg;
    }

    @Override
    public void registerAll() {
        deferredReg.register(ForgeHexInitializer.getModEventBus());
    }

    @Override
    public <T extends B> Supplier<T> register(String id, Supplier<T> provider) {
        return deferredReg.register(id, provider);
    }

    @Override
    public <T extends B> Holder<B> registerHolder(String id, Supplier<T> provider) {
        return deferredReg.register(id, provider);
    }
}
