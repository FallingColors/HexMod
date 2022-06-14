package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota

object OpModifyInPlace : ConstManaAction {
    override val argc = 3
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val list = args.getList(0, argc)
        val index = args.getPositiveIntUnder(1, list.size(), argc)
        val iota = args[2]
        return list.modifyAt(index) { SpellList.LPair(iota, it.cdr) }.asActionResult
    }
}
