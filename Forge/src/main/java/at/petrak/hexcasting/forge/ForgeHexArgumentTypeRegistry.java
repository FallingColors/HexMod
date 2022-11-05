package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.command.PatternResLocArgument;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// ArgumentTypeInfos.java
public class ForgeHexArgumentTypeRegistry {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(
        ForgeRegistries.COMMAND_ARGUMENT_TYPES, HexAPI.MOD_ID);

    // how fucking ergonomic
    public static final RegistryObject<ArgumentTypeInfo<PatternResLocArgument, SingletonArgumentInfo<PatternResLocArgument>.Template>>
        PATTERN_RESLOC = register(PatternResLocArgument.class,
        "pattern",
        SingletonArgumentInfo.contextFree(PatternResLocArgument::id)
    );

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>>
    RegistryObject<ArgumentTypeInfo<A, T>> register(
        Class<A> clazz,
        String name,
        ArgumentTypeInfo<A, T> ati) {
        var robj = ARGUMENT_TYPES.register(name, () -> ati);
        ArgumentTypeInfos.registerByClass(clazz, ati);
        return robj;
    }
}
