package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import kotlin.math.roundToInt

object OpIndex : ConstManaOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<List<SpellDatum<*>>>(0).toMutableList()
        val index = args.getChecked<Double>(1)
        val x = list.getOrElse(index.roundToInt()) { SpellDatum.make(Widget.NULL) }
        return listOf(x)
    }
}