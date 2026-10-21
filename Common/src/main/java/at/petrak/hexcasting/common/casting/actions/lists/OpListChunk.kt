package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveIntUnderInclusive
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota

object OpListChunk : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc)
        val chunkSize = args.getPositiveIntUnderInclusive(1, maxOf(1, list.size), argc)

        val chunks = mutableListOf<Iota>()
        if (chunkSize <= 0 || list.isEmpty()) {
            return chunks.asActionResult
        }

        var i = 0
        while (i < list.size) {
            val end = minOf(i + chunkSize, list.size)
            chunks.add(ListIota(list.subList(i, end)))
            i = end
        }

        return chunks.asActionResult
    }
}
