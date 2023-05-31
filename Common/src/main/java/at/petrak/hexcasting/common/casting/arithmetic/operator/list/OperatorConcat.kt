package at.petrak.hexcasting.common.casting.arithmetic.operator.list

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.casting.arithmetic.operator.nextList
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*

object OperatorConcat : Operator(2, IotaMultiPredicate.all(IotaPredicate.ofType(LIST))) {
    override fun apply(iotas: Iterable<Iota>): Iterable<Iota> {
        val it = iotas.iterator().withIndex()
        val lhs = it.nextList(arity).toMutableList()
        val rhs = it.nextList(arity)
        lhs.addAll(rhs)
        return lhs.asActionResult
    }
}