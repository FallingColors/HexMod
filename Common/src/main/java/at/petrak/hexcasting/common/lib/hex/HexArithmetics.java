package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.engine.ArithmeticEngine;
import at.petrak.hexcasting.common.casting.arithmetic.*;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexArithmetics {
    private static final IXplatRegister<Arithmetic> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(HexRegistries.ARITHMETIC);

    private static ArithmeticEngine ENGINE;

    public static void register() {
        REGISTER.registerAll();
    }

    public static ArithmeticEngine getEngine() {
        if (ENGINE == null) {
            ENGINE = new ArithmeticEngine(REGISTRY.holders().map(Holder.Reference::value).collect(Collectors.toList()));
        }
        return ENGINE;
    }

    public static final Registry<Arithmetic> REGISTRY = IXplatAbstractions.INSTANCE.getArithmeticRegistry();

    public static Supplier<DoubleArithmetic> DOUBLE = REGISTER.register("double", () -> DoubleArithmetic.INSTANCE);
    public static Supplier<Vec3Arithmetic> VEC3 = REGISTER.register("vec3", () -> Vec3Arithmetic.INSTANCE);
    public static Supplier<ListArithmetic> LIST = REGISTER.register("list", () -> ListArithmetic.INSTANCE);
    public static Supplier<BoolArithmetic> BOOL = REGISTER.register("bool", () -> BoolArithmetic.INSTANCE);
    public static Supplier<ListSetArithmetic> LIST_SET = REGISTER.register("list_set", () -> ListSetArithmetic.INSTANCE);
    public static Supplier<BitwiseSetArithmetic> BITWISE_SET = REGISTER.register("bitwise_set", () -> BitwiseSetArithmetic.INSTANCE);
}
