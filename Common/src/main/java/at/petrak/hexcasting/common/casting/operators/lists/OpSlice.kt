package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getList
import at.petrak.hexcasting.api.spell.getPositiveIntUnder
import at.petrak.hexcasting.api.spell.iota.Iota
import kotlin.math.max
import kotlin.math.min

object OpSlice : ConstManaAction {
    override val argc = 3
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val list = args.getList(0, argc).toList()
        val index1 = args.getPositiveIntUnder(1, list.size, argc)
        val index2 = args.getPositiveIntUnder(2, list.size, argc)

        if (index1 == index2)
            return emptyList<Iota>().asActionResult

        return list.subList(min(index1, index2), max(index1, index2)).asActionResult
    }
}
