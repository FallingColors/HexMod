package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget

object OpAnd : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        return listOf(
            if (args[0].payload == Widget.NULL)
                SpellDatum.make(Widget.NULL)
            else
                args[1]
        )
    }
}