package at.petrak.hexcasting.common.casting.arithmetic.operator

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapDivideByZero
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.DOUBLE
import kotlin.math.log

object OperatorLog : Operator(2, IotaMultiPredicate.all(IotaPredicate.ofType(DOUBLE))) {
    override fun apply(iotas: Iterable<Iota>, env : CastingEnvironment): Iterable<Iota> {
        val it = iotas.iterator().withIndex()
        val value = it.nextDouble(arity)
        val base = it.nextDouble(arity)
        if (value <= 0.0 || base <= 0.0 || base == 1.0)
            throw MishapDivideByZero.of(iotas.first(), iotas.last(), "logarithm")
        return log(value, base).asActionResult
    }
}