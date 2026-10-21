package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota

object OpListFlatten : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc)
        val flattened = mutableListOf<Iota>()
        for (iota in list) {
            if (iota is ListIota) {
                flattened.addAll(iota.list)
            } else {
                flattened.add(iota)
            }
        }
        return flattened.asActionResult
    }
}
