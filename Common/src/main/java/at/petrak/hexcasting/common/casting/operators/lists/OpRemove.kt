package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getList
import at.petrak.hexcasting.api.spell.getPositiveIntUnder
import at.petrak.hexcasting.api.spell.iota.Iota

object OpRemove : ConstManaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val list = args.getList(0, argc).toMutableList()
        val index = args.getPositiveIntUnder(1, list.size, argc)
        list.removeAt(index)
        return list.asActionResult
    }
}
