package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext

object OpReverski : ConstManaOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        return spellListOf(args.getChecked<List<SpellDatum<*>>>(0).asReversed()) // okay kotlin kinda pogged for this
    }
}