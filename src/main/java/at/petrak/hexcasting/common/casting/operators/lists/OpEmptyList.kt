package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext

object OpEmptyList : ConstManaOperator {
    override val argc = 0
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        return spellListOf(emptyList<SpellDatum<*>>()) // sorry for taking all the easy impls, hudeler
    }
}