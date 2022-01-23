package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext

object OpAppend : ConstManaOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<List<SpellDatum<*>>>(0).toMutableList()
        val datum = args[1]
        list.add(datum)
        return spellListOf(list)
    }
}