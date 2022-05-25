package at.petrak.hexcasting.common.casting.operators.math.bit

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import kotlin.math.roundToInt

object OpAnd : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val firstParam = numOrList(args[0], 0)

        if (firstParam.right().isPresent) {
            val list1 = firstParam.right().get()
            val list2 = args.getChecked<SpellList>(1, argc)
            return list1.filter { x -> list2.any(x::tolerantEquals) }.asSpellResult
        }

        val num1 = firstParam.left().get().roundToInt()
        val num2 = args.getChecked<Double>(1, argc).roundToInt()
        return (num1 and num2).asSpellResult
    }
}
