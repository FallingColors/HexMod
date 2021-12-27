package at.petrak.hex.common.casting.operators

import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.SpellOperator.Companion.getChecked
import at.petrak.hex.common.casting.SpellOperator.Companion.spellListOf

object OpSwap : SimpleOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val a = args.getChecked<Any>(0)
        val b = args.getChecked<Any>(1)
        return spellListOf(b, a)
    }
}