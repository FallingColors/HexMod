package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import kotlin.math.roundToInt

object OpIndex : ConstManaOperator {
    override val argc = 2
    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        val list = args.getChecked<SpellList>(0, argc).toMutableList()
        val index = args.getChecked<Double>(1, argc)
        val x = list.getOrElse(index.roundToInt()) { LegacySpellDatum.make(Widget.NULL) }
        return listOf(x)
    }
}
