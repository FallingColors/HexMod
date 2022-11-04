package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.command.PatternResLocArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// ArgumentTypeInfos.java
public class ForgeHexArgumentTypeRegistry {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(
        Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, HexAPI.MOD_ID);

    public static final RegistryObject<ArgumentTypeInfo<PatternResLocArgument, ?>> PATTERN_RESLOC = ARGUMENT_TYPES.register(
        "pattern", () ->
            SingletonArgumentInfo.contextFree(PatternResLocArgument::id));
}
