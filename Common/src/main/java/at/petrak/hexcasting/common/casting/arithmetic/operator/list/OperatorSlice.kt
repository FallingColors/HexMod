package at.petrak.hexcasting.common.casting.arithmetic.operator.list

import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBasic
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector
import at.petrak.hexcasting.common.casting.arithmetic.operator.nextList
import at.petrak.hexcasting.common.casting.arithmetic.operator.nextPositiveIntUnderInclusive
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*
import kotlin.math.max
import kotlin.math.min

object OperatorSlice : OperatorBasic(3, IotaMultiPredicate.triple(IotaPredicate.ofType(LIST), IotaPredicate.ofType(DOUBLE), IotaPredicate.ofType(DOUBLE))) {
    override fun apply(iotas: Iterable<Iota>, env: CastingEnvironment): Iterable<Iota> {
        val it = iotas.iterator().withIndex()
        val list = it.nextList(arity)
        val index0 = it.nextPositiveIntUnderInclusive(list.size, arity)
        val index1 = it.nextPositiveIntUnderInclusive(list.size, arity)

        if (index0 == index1)
            return Vector.empty<Iota>().asActionResult
        return Vector.from(list.subList(min(index0, index1), max(index0, index1))).asActionResult
    }
}