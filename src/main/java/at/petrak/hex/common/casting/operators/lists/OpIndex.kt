package at.petrak.hex.common.casting.operators.lists

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.Operator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.Widget
import kotlin.math.roundToInt

object OpIndex : ConstManaOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<List<SpellDatum<*>>>(0).toMutableList()
        val index = args.getChecked<Double>(1)
        val x = list.getOrElse(index.roundToInt()) { SpellDatum.make(Widget.NULL) }
        return spellListOf(x)
    }
}