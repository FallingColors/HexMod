package at.petrak.hexcasting.common.casting.arithmetic.operator.list

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.casting.arithmetic.operator.nextList
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*

object OperatorAppend : Operator(2, IotaMultiPredicate.pair(IotaPredicate.ofType(LIST), IotaPredicate.TRUE)) {
    override fun apply(iotas: Iterable<Iota>): Iterable<Iota> {
        val it = iotas.iterator().withIndex()
        val list = it.nextList(arity).toMutableList()
        list.add(it.next().value)
        return list.asActionResult
    }
}