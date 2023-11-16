package at.petrak.hexcasting.common.casting.arithmetic.operator.list

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.common.casting.arithmetic.operator.nextList
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.LIST

object OperatorUnCons : Operator(1, IotaMultiPredicate.all(IotaPredicate.ofType(LIST))) {
    override fun apply(iotas: Iterable<Iota>): Iterable<Iota> {
        val it = iotas.iterator().withIndex()
        val list = it.nextList(arity)
        if (list.nonEmpty) {
            return listOf(ListIota(list.cdr), list.car)
        }
        return listOf(ListIota(list), NullIota())
    }
}