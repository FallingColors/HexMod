package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota

object OpListInterleave : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list1 = args.getList(0, argc)
        val list2 = args.getList(1, argc)

        val interleaved = mutableListOf<Iota>()
        val maxSize = maxOf(list1.size, list2.size)
        for (i in 0 until maxSize) {
            if (i < list1.size) {
                interleaved.add(list1[i])
            }
            if (i < list2.size) {
                interleaved.add(list2[i])
            }
        }
        return interleaved.asActionResult
    }
}
