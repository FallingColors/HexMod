package at.petrak.hexcasting.common.casting.arithmetic.operator.list

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.casting.arithmetic.operator.nextList
import at.petrak.hexcasting.common.casting.arithmetic.operator.nextPositiveIntUnder
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*

object OperatorReplace : Operator(3, IotaMultiPredicate.triple(IotaPredicate.ofType(LIST), IotaPredicate.ofType(DOUBLE), IotaPredicate.TRUE)) {
    override fun apply(iotas: Iterable<Iota>, env: CastingEnvironment): Iterable<Iota> {
        val it = iotas.iterator().withIndex()
        val list = it.nextList(arity)
        val index = it.nextPositiveIntUnder(list.size(), arity)
        val iota = it.next().value
        return list.modifyAt(index) { SpellList.LPair(iota, it.cdr) }.asActionResult
    }
}