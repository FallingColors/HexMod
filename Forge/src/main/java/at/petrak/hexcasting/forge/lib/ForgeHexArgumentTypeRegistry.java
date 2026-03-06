package at.petrak.hexcasting.forge.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.command.PatternResLocArgument;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ForgeHexArgumentTypeRegistry {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(
        BuiltInRegistries.COMMAND_ARGUMENT_TYPE, HexAPI.MOD_ID);

    // how fucking ergonomic
    public static final DeferredHolder<ArgumentTypeInfo<?, ?>,
        ArgumentTypeInfo<PatternResLocArgument, SingletonArgumentInfo<PatternResLocArgument>.Template>>
        PATTERN_RESLOC = register(PatternResLocArgument.class,
        "pattern",
        SingletonArgumentInfo.contextFree(PatternResLocArgument::id)
    );

    @SuppressWarnings("unchecked")
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>>
    DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<A, T>> register(
        Class<A> clazz,
        String name,
        ArgumentTypeInfo<A, T> ati) {
        var holder = ARGUMENT_TYPES.register(name, () -> ati);
        ArgumentTypeInfos.registerByClass(clazz, ati);
        return (DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<A, T>>) (Object) holder;
    }
}
