package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.arithmetic.engine.ArithmeticEngine;
import at.petrak.hexcasting.common.casting.arithmetic.DoubleArithmetic;
import at.petrak.hexcasting.common.casting.arithmetic.ListArithmetic;
import at.petrak.hexcasting.common.casting.arithmetic.Vec3Arithmetic;

import java.util.List;

public class HexArithmetics {
    public static ArithmeticEngine ENGINE = new ArithmeticEngine(List.of(DoubleArithmetic.INSTANCE, Vec3Arithmetic.INSTANCE, ListArithmetic.INSTANCE));
}
