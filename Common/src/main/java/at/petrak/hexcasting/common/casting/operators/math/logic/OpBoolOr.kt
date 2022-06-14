package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpBoolOr : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return listOf(
            if (args[0].payload == Widget.NULL)
                args[1]
            else
                args[0]
        )
    }
}
