package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import kotlin.math.roundToInt

object OpModifyInPlace : ConstManaOperator {
    override val argc = 3
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<List<SpellDatum<*>>>(0)
        val index = args.getChecked<Double>(1).roundToInt()
        val iota = args[2]

        if (0 > index || index > list.size)
            return spellListOf(list)


        val newList = list.toMutableList()
        if (index == list.size)
            newList.add(iota)
        else
            newList[index] = iota

        return spellListOf(newList)
    }
}
