package at.petrak.hex.lib;

import at.petrak.hex.HexMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegisterHelper {
    // yoinked from botnia
    public static <V extends IForgeRegistryEntry<V>> void register(IForgeRegistry<V> reg, ResourceLocation name,
            IForgeRegistryEntry<V> thing) {
        reg.register(thing.setRegistryName(name));
    }

    public static <V extends IForgeRegistryEntry<V>> void register(IForgeRegistry<V> reg, String name,
            IForgeRegistryEntry<V> thing) {
        register(reg, prefix(name), thing);
    }

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(HexMod.MOD_ID, path);
    }
}
