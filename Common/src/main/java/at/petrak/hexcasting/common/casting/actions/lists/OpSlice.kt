package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveIntUnderInclusive
import at.petrak.hexcasting.api.casting.iota.Iota
import kotlin.math.max
import kotlin.math.min

object OpSlice : ConstMediaAction {
    override val argc = 3
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc).toList()
        val index1 = args.getPositiveIntUnderInclusive(1, list.size, argc)
        val index2 = args.getPositiveIntUnderInclusive(2, list.size, argc)

        if (index1 == index2)
            return emptyList<Iota>().asActionResult

        return list.subList(min(index1, index2), max(index1, index2)).asActionResult
    }
}
