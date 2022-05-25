package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Widget
import kotlin.math.roundToInt

object OpIndex : ConstManaOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<SpellList>(0, argc).toMutableList()
        val index = args.getChecked<Double>(1, argc)
        val x = list.getOrElse(index.roundToInt()) { SpellDatum.make(Widget.NULL) }
        return listOf(x)
    }
}
