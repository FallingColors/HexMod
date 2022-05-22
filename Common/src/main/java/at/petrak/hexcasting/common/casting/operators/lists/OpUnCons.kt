package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpUnCons : ConstManaOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<SpellList>(0, argc)
        if (list.nonEmpty) {
            return spellListOf(list.cdr, list.car)
        }
        return spellListOf(list, Widget.NULL)
    }
}
