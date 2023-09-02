package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpConcat : ConstManaOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = args.getChecked<SpellList>(0, argc).toMutableList()
        val rhs = args.getChecked<SpellList>(1, argc)
        lhs.addAll(rhs)
        return lhs.asSpellResult
    }
}
