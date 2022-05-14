package at.petrak.hexcasting.common.casting.operators.math.bit

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.common.casting.operators.math.MathOpUtils
import kotlin.math.roundToInt

object OpXor : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val firstParam = MathOpUtils.GetNumOrList(args[0], 0)

        if (firstParam.right().isPresent) {
            val list1 = firstParam.right().get()
            val list2 = args.getChecked<SpellList>(1)
            return spellListOf(list1.filter { it !in list2 } + list2.filter { it !in list1 })
        }

        val num1 = firstParam.left().get().roundToInt()
        val num2 = args.getChecked<Double>(1).roundToInt()
        return spellListOf((num1 xor num2).toDouble())
    }
}
