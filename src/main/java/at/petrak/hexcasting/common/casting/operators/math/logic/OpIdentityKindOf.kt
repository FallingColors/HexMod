package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget

object OpIdentityKindOf : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        return spellListOf(
            when (args[0].payload) {
                Widget.NULL -> 0.0
                0.0 -> Widget.NULL
                else -> args[0].payload
            }
        )
    }
}