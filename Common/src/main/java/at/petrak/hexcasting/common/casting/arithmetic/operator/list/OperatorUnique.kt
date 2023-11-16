package at.petrak.hexcasting.common.casting.arithmetic.operator.list

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.common.casting.arithmetic.operator.nextList
import at.petrak.hexcasting.common.casting.actions.math.bit.OpToSet
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.LIST

object OperatorUnique : Operator(1, IotaMultiPredicate.all(IotaPredicate.ofType(LIST))) {
    override fun apply(iotas: Iterable<Iota>): Iterable<Iota> {
        val it = iotas.iterator().withIndex()
        val list = it.nextList(OpToSet.argc)
        val out = mutableListOf<Iota>()

        for (subiota in list) {
            if (out.none { Iota.tolerates(it, subiota) }) {
                out.add(subiota)
            }
        }

        return out.asActionResult
    }
}