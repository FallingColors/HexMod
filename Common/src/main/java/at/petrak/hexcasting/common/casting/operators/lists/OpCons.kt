package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getList
import at.petrak.hexcasting.api.spell.iota.Iota

object OpCons : ConstManaAction {
    override val argc = 2
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val bottom = args.getList(0, argc)
        val top = args[1]
        return SpellList.LPair(top, bottom).asActionResult
    }
}
