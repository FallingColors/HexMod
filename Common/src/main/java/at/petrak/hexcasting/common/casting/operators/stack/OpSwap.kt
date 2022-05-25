package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.spellListOf
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpSwap : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        val a = args.getChecked<Any>(0, argc)
        val b = args.getChecked<Any>(1, argc)
        return spellListOf(b, a)
    }
}
