package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.tolerantEquals

class OpEquality(val invert: Boolean) : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        val lhs = args[0]
        val rhs = args[1]

        return (lhs.tolerantEquals(rhs) != invert).asSpellResult
    }
}
