package at.petrak.hexcasting.common.casting.arithmetic.operator.list

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.casting.arithmetic.operator.nextList
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*

object OperatorIndexOf : Operator(2, IotaMultiPredicate.pair(IotaPredicate.ofType(LIST), IotaPredicate.TRUE)) {
    override fun apply(iotas: Iterable<Iota>, env: CastingEnvironment): Iterable<Iota> {
        val it = iotas.iterator().withIndex()
        val list = it.nextList(arity).toList()
        val toFind = it.next().value
        return list.indexOfFirst { Iota.tolerates(toFind, it) }.asActionResult
    }
}