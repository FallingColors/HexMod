package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getList
import at.petrak.hexcasting.api.spell.iota.Iota

object OpConcat : ConstManaAction {
    override val argc = 2
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val lhs = args.getList(0, argc).toMutableList()
        val rhs = args.getList(1, argc)
        lhs.addAll(rhs)
        return lhs.asActionResult
    }
}
