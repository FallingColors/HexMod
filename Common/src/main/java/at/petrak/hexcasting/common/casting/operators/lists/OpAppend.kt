package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota

object OpAppend : ConstMediaAction {
    override val argc = 2
    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc).toMutableList()
        val datum = args[1]
        list.add(datum)
        return list.asActionResult
    }
}
