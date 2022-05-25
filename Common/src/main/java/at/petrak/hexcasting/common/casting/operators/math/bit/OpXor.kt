package at.petrak.hexcasting.common.casting.operators.math.bit

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import kotlin.math.roundToInt

object OpXor : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val firstParam = numOrList(args[0], 0)

        if (firstParam.right().isPresent) {
            val list1 = firstParam.right().get()
            val list2 = args.getChecked<SpellList>(1, argc)
            val out = list1.filter { x1 -> list2.none(x1::tolerantEquals) } + list2.filter { x2 -> list1.none(x2::tolerantEquals) }
            return out.asSpellResult
        }

        val num1 = firstParam.left().get().roundToInt()
        val num2 = args.getChecked<Double>(1, argc).roundToInt()
        return (num1 xor num2).asSpellResult
    }
}
