package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.utils.Vector

object OpUnCons : ConstMediaAction {
    override val argc = 1
    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = SpellList.LList(0, args.getList(0, argc))
        if (list.nonEmpty) {
            return listOf(ListIota(Vector.from(list.cdr)), list.car)
        }
        return listOf(args[0], NullIota())
    }
}
