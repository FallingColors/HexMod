package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota

object OpListZip : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list1 = args.getList(0, argc)
        val list2 = args.getList(1, argc)
        val minSize = minOf(list1.size, list2.size)
        val zipped = mutableListOf<List<Iota>>()
        for (i in 0 until minSize) {
            zipped.add(listOf(list1[i], list2[i]))
        }
        return zipped.asActionResult
    }
}
