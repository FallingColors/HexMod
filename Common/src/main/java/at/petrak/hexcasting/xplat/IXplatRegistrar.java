package at.petrak.hexcasting.xplat;

import net.minecraft.core.Holder;

import java.util.function.Supplier;

/**
 * A platform-agnostic registrar for type {B} <br>
 * FabricRegistrar registers directly while ForgeRegistrar uses deferred registry instead
 * @param <B> The Type the IXplatRegistrar is registering
 */
public interface IXplatRegistrar<B> {

    <T extends B> Supplier<T> register(String id, Supplier<T> provider);

    <T extends B> Holder<B> registerHolder(String id, Supplier<T> provider);

    /**
     * Call from mod initializer to actually register all entries in this register
     */
    void registerAll();
}