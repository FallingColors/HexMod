package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveIntUnderInclusive
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota

object OpListWindows : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc)
        val windowSize = args.getPositiveIntUnderInclusive(1, maxOf(1, list.size), argc)

        val windows = mutableListOf<Iota>()
        if (windowSize <= 0 || list.isEmpty() || windowSize > list.size) {
            return windows.asActionResult
        }

        for (i in 0..list.size - windowSize) {
            val subList = list.subList(i, i + windowSize)
            windows.add(ListIota(subList))
        }

        return windows.asActionResult
    }
}
