package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Widget
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object OpSlice : ConstManaOperator {
    override val argc = 3
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<List<SpellDatum<*>>>(0)
        val index1 = max(0, args.getChecked<Double>(1).roundToInt())
        val index2 = min(args.getChecked<Double>(2).roundToInt(), list.size)

        return spellListOf(list.subList(index1, index2))
    }
}
