package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator.Companion.getChecked
import at.petrak.hex.api.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum

object OpDuplicate : SimpleOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val datum = args.getChecked<Any>(0)
        return spellListOf(datum, datum)
    }
}