package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveIntUnder
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpModifyInPlace : ConstMediaAction {
    override val argc = 3
    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val list = args.getList(0, argc)
        val index = args.getPositiveIntUnder(1, list.size, argc)
        val iota = args[2]
        return list.updated(index, iota).asActionResult
    }
}
