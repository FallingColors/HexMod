package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.engine.ArithmeticEngine;
import at.petrak.hexcasting.common.casting.arithmetic.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexArithmetics {
    private static ArithmeticEngine ENGINE;

    public static ArithmeticEngine getEngine() {
        if (ENGINE == null) {
            ENGINE = new ArithmeticEngine(REGISTRY.holders().map(Holder.Reference::value).collect(Collectors.toList()));
        }
        return ENGINE;
    }

    public static final Registry<Arithmetic> REGISTRY = IXplatAbstractions.INSTANCE.getArithmeticRegistry();

    public static void register(BiConsumer<Arithmetic, ResourceLocation> r) {
        for (var e : ARITHMETICS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, Arithmetic> ARITHMETICS = new LinkedHashMap<>();

    public static DoubleArithmetic DOUBLE = make("double", DoubleArithmetic.INSTANCE);
    public static Vec3Arithmetic VEC3 = make("vec3", Vec3Arithmetic.INSTANCE);
    public static ListArithmetic LIST = make("list", ListArithmetic.INSTANCE);
    public static BoolArithmetic BOOL = make("bool", BoolArithmetic.INSTANCE);
    public static ListSetArithmetic LIST_SET = make("list_set", ListSetArithmetic.INSTANCE);
    public static BitwiseSetArithmetic BITWISE_SET = make("bitwise_set", BitwiseSetArithmetic.INSTANCE);

    private static <T extends Arithmetic> T make(String name, T arithmetic) {
        var old = ARITHMETICS.put(modLoc(name), arithmetic);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return arithmetic;
    }
}
