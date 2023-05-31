package at.petrak.hexcasting.common.casting.arithmetic

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic.*
import at.petrak.hexcasting.api.casting.arithmetic.engine.InvalidOperatorException
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBinary
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*
import it.unimi.dsi.fastutil.booleans.BooleanBinaryOperator
import it.unimi.dsi.fastutil.booleans.BooleanUnaryOperator
import java.util.function.BiFunction

object BoolArithmetic : Arithmetic {
    private val OPS = listOf(
        AND,
        OR,
        XOR,
        GREATER,
        LESS,
        GREATER_EQ,
        LESS_EQ,
        NOT,
        ABS
    )

    override fun arithName(): String = "bool_math"

    override fun opTypes() = OPS

    override fun getOperator(pattern: HexPattern): Operator = when (pattern) {
        AND -> make2 { a, b -> a and b }
        OR -> make2 { a, b -> a or b }
        XOR -> make2 { a, b -> a xor b }
        GREATER -> makeComp { x, y -> x > y }
        LESS -> makeComp { x, y -> x < y }
        GREATER_EQ -> makeComp { x, y -> DoubleIota.tolerates(x, y) || x >= y }
        LESS_EQ -> makeComp { x, y -> DoubleIota.tolerates(x, y) || x <= y }
        NOT -> make1 { a -> !a }
        ABS -> OperatorUnary(ALL_BOOLS) { i: Iota -> DoubleIota( if (Operator.downcast(i, BOOLEAN).bool) 1.0 else 0.0 ) }
        else -> throw InvalidOperatorException("$pattern is not a valid operator in Arithmetic $this.")
    }
    val ALL_BOOLS: IotaMultiPredicate = IotaMultiPredicate.all(IotaPredicate.ofType(BOOLEAN))

    private fun make1(op: BooleanUnaryOperator): OperatorUnary {
        return OperatorUnary(ALL_BOOLS) { i: Iota -> BooleanIota(op.apply(Operator.downcast(i, BOOLEAN).bool)) }
    }
    private fun make2(op: BooleanBinaryOperator): OperatorBinary {
        return OperatorBinary(ALL_BOOLS) { i: Iota, j: Iota -> BooleanIota(op.apply(Operator.downcast(i, BOOLEAN).bool, Operator.downcast(j, BOOLEAN).bool)) }
    }
    private fun makeComp(op: BiFunction<Double, Double, Boolean>): OperatorBinary {
        return OperatorBinary(DoubleArithmetic.ACCEPTS)
            { i: Iota, j: Iota -> BooleanIota(op.apply(Operator.downcast(i, DOUBLE).double, Operator.downcast(j, DOUBLE).double)) }
    }
}