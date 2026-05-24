package at.petrak.hexcasting.forge.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.command.PatternResKeyArgument;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// ArgumentTypeInfos.java
public class ForgeHexArgumentTypeRegistry {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(
        Registries.COMMAND_ARGUMENT_TYPE, HexAPI.MOD_ID);

    // how fucking ergonomic
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<PatternResKeyArgument,
            SingletonArgumentInfo<PatternResKeyArgument>.Template>>
        PATTERN_RESLOC = register(PatternResKeyArgument.class,
        "pattern",
        SingletonArgumentInfo.contextFree(PatternResKeyArgument::id)
    );

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>>
    DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<A, T>> register(
        Class<A> clazz,
        String name,
        ArgumentTypeInfo<A, T> ati) {
        var robj = ARGUMENT_TYPES.register(name, () -> ati);
        ArgumentTypeInfos.registerByClass(clazz, ati);
        return robj;
    }
}
