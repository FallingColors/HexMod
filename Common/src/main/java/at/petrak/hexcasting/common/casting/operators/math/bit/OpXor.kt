package at.petrak.hexcasting.common.casting.operators.math.bit

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.DatumType
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import kotlin.math.roundToInt

object OpXor : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val type = args[0].getType()

        if (type == DatumType.LIST) {
            val list1 = args.getChecked<List<SpellDatum<*>>>(0)
            val list2 = args.getChecked<List<SpellDatum<*>>>(1)
            return spellListOf(list1.filter { it !in list2 } + list2.filter { it !in list1 })
        }

        val num1 = args.getChecked<Double>(0).roundToInt()
        val num2 = args.getChecked<Double>(1).roundToInt()
        return spellListOf((num1 xor num2).toDouble())
    }
}
