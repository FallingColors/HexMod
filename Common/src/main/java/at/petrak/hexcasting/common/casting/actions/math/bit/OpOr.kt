package at.petrak.hexcasting.common.casting.actions.math.bit

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getLong
import at.petrak.hexcasting.api.casting.getLongOrList
import at.petrak.hexcasting.api.casting.iota.Iota

object OpOr : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val firstParam = args.getLongOrList(0, argc)

        return firstParam.map(
            { num1 ->
                val num2 = args.getLong(1, argc)
                (num1 or num2).asActionResult
            },
            { list1 ->
                val list2 = args.getList(1, argc)
                (list1 + list2.filter { x -> list1.none { Iota.tolerates(x, it) } }).asActionResult
            }
        )
    }
}
