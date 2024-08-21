package at.petrak.hexcasting.common.casting.arithmetic

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic
import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic.*
import at.petrak.hexcasting.api.casting.arithmetic.engine.InvalidOperatorException
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBinary
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.casting.mishaps.MishapDivideByZero
import at.petrak.hexcasting.common.casting.arithmetic.operator.OperatorLog
import at.petrak.hexcasting.common.casting.arithmetic.operator.asDoubleBetween
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import java.util.function.DoubleBinaryOperator
import java.util.function.DoubleUnaryOperator
import kotlin.math.*

object DoubleArithmetic : Arithmetic {
    @JvmField
    val OPS = listOf(
        ADD,
        SUB,
        MUL,
        DIV,
        ABS,
        POW,
        FLOOR,
        CEIL,
        SIN,
        COS,
        TAN,
        ARCSIN,
        ARCCOS,
        ARCTAN,
        ARCTAN2,
        LOG,
        MOD
    )

    /**
     * An example of an IotaMultiPredicate, which returns true only if all arguments to the Operator are DoubleIotas.
     */
    val ACCEPTS: IotaMultiPredicate = IotaMultiPredicate.all(IotaPredicate.ofType(HexIotaTypes.DOUBLE))

    override fun arithName() = "double_math"

    override fun opTypes() = OPS

    override fun getOperator(pattern: HexPattern): Operator {
        return when (pattern) {
            ADD     -> make2 { a, b -> a + b }
            SUB     -> make2 { a, b -> a - b }
            MUL     -> make2 { a, b -> a * b }
            DIV     -> make2 { a, b -> if (b == 0.0) throw MishapDivideByZero.of(a, b) else a / b }
            ABS     -> make1 { a -> abs(a) }
            // throw MishapDivideByZero if raising a negative number to a fractional power (ie. sqrt(-1) etc)
            POW     -> make2 { a, b -> if (a < 0 && !DoubleIota.tolerates(floor(b), b)) throw MishapDivideByZero.of(a, b, "exponent") else a.pow(b) }
            FLOOR   -> make1 { a -> floor(a) }
            CEIL    -> make1 { a -> ceil(a) }
            SIN     -> make1 { a -> sin(a) }
            COS     -> make1 { a -> cos(a) }
            TAN     -> make1 { a -> if (cos(a) == 0.0) throw MishapDivideByZero.tan(a) else tan(a) }
            ARCSIN  -> make1 { a -> asin(a.asDoubleBetween(-1.0, 1.0, 0)) }
            ARCCOS  -> make1 { a -> acos(a.asDoubleBetween(-1.0, 1.0, 0)) }
            ARCTAN  -> make1 { a -> atan(a) }
            ARCTAN2 -> make2 { a, b -> atan2(a, b) }
            LOG     -> OperatorLog
            MOD     -> make2 { a, b -> if (b == 0.0) throw MishapDivideByZero.of(a, b) else a % b }
            else    -> throw InvalidOperatorException("$pattern is not a valid operator in Arithmetic $this.")
        }
    }

    fun make1(op: DoubleUnaryOperator) = OperatorUnary(ACCEPTS)
        { i: Iota -> DoubleIota(op.applyAsDouble(Operator.downcast(i, HexIotaTypes.DOUBLE).double)) }

    fun make2(op: DoubleBinaryOperator) = OperatorBinary(ACCEPTS)
        { i: Iota, j: Iota -> DoubleIota(op.applyAsDouble(Operator.downcast(i, HexIotaTypes.DOUBLE).double, Operator.downcast(j, HexIotaTypes.DOUBLE).double)) }
}
