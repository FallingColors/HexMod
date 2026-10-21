package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getInt
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota

object OpListRotate : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc)
        val shift = args.getInt(1, argc)

        if (list.isEmpty()) {
            return list.asActionResult
        }

        val size = list.size
        // Normalize shift using modulo
        var k = shift % size
        if (k < 0) {
            k += size
        }

        if (k == 0) {
            return list.asActionResult
        }

        val rotated = mutableListOf<Iota>()
        // Right rotation by k means elements from size - k onwards come first
        for (i in (size - k) until size) {
            rotated.add(list[i])
        }
        for (i in 0 until (size - k)) {
            rotated.add(list[i])
        }

        return rotated.asActionResult
    }
}
