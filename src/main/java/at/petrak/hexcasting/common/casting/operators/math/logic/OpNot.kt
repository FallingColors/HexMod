package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget

object OpNot : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val falsy = args[0].payload == Widget.NULL || args[0].payload == 0.0
        return spellListOf(if (falsy) 1.0 else 0.0)
    }
}