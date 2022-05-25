package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpBoolXor : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        return listOf(
            if (args[0].payload != Widget.NULL && args[1].payload == Widget.NULL)
                args[0]
            else if (args[0].payload == Widget.NULL && args[1].payload != Widget.NULL)
                args[1]
            else
                LegacySpellDatum.make(Widget.NULL)
        )
    }
}
