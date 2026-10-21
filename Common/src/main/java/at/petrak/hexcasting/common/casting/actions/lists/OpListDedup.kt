package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota

object OpListDedup : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc)
        if (list.isEmpty()) {
            return list.asActionResult
        }
        val deduped = mutableListOf<Iota>()
        deduped.add(list[0])
        for (i in 1 until list.size) {
            val prev = list[i - 1]
            val curr = list[i]
            if (!prev.toleratesOther(curr) && prev != curr) {
                deduped.add(curr)
            }
        }
        return deduped.asActionResult
    }
}
