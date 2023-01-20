package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota

object OpUnCons : ConstMediaAction {
    override val argc = 1
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val list = args.getList(0, argc)
        if (list.nonEmpty) {
            return listOf(ListIota(list.cdr), list.car)
        }
        return listOf(args[0], NullIota())
    }
}
