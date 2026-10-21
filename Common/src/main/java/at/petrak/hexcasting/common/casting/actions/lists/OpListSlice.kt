package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapIndexOutOfBounds
import kotlin.math.max
import kotlin.math.min

object OpListSlice : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc).toList()
        val fromIdx = args.getInt(1, argc)
        val toIdx = args.getInt(2, argc)

        if (fromIdx < 0 || fromIdx > list.size || toIdx < 0 || toIdx > list.size) {
            throw MishapIndexOutOfBounds(fromIdx, list.size, 0)
        }

        val start = min(fromIdx, toIdx)
        val end = max(fromIdx, toIdx)

        return list.subList(start, end).asActionResult
    }
}
