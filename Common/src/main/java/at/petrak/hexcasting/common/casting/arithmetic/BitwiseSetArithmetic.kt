package at.petrak.hexcasting.common.casting.arithmetic

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic.*
import at.petrak.hexcasting.api.casting.arithmetic.engine.InvalidOperatorException
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator.downcast
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBinary
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.DOUBLE
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator
import kotlin.math.roundToLong

object BitwiseSetArithmetic : Arithmetic {
    private val OPS = listOf(
        AND,
        OR,
        XOR,
        NOT
    )

    override fun arithName() = "bitwise_set_ops"

    override fun opTypes() = OPS

    override fun getOperator(pattern: HexPattern): Operator = when (pattern) {
        AND -> make2 { x, y -> x and y }
        OR -> make2 { x, y -> x or y }
        XOR -> make2 { x, y -> x xor y }
        NOT -> make1 { x -> x.inv() }
        else -> throw InvalidOperatorException("$pattern is not a valid operator in Arithmetic $this.")
    }

    private fun make1(op: LongUnaryOperator): OperatorUnary = OperatorUnary(DoubleArithmetic.ACCEPTS)
        { i: Iota -> DoubleIota(op.applyAsLong(downcast(i, DOUBLE).double.roundToLong()).toDouble()) }

    private fun make2(op: LongBinaryOperator): OperatorBinary = OperatorBinary(DoubleArithmetic.ACCEPTS)
        { i: Iota, j: Iota -> DoubleIota(
                op.applyAsLong(
                    downcast(i, DOUBLE).double.roundToLong(),
                    downcast(j, DOUBLE).double.roundToLong()
                ).toDouble()) }
}