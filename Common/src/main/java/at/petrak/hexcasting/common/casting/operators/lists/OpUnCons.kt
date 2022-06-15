package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getList
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import at.petrak.hexcasting.api.spell.iota.NullIota

object OpUnCons : ConstManaAction {
    override val argc = 1
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val list = args.getList(0, argc)
        if (list.nonEmpty) {
            return listOf(ListIota(list.cdr), list.car)
        }
        return listOf(args[0], NullIota())
    }
}
