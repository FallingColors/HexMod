package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.arithmetic.engine.ArithmeticEngine;
import at.petrak.hexcasting.api.casting.arithmetic.impls.DoubleArithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.impls.Vec3Arithmetic;

public class HexArithmetics {
    public static ArithmeticEngine ENGINE = new ArithmeticEngine(DoubleArithmetic.INSTANCE, Vec3Arithmetic.INSTANCE);
}
