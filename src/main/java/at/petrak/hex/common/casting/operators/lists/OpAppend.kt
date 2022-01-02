package at.petrak.hex.common.casting.operators.lists

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.Operator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum

object OpAppend : ConstManaOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<List<SpellDatum<*>>>(0).toMutableList()
        val datum = args[1]
        list.add(datum)
        return spellListOf(list)
    }
}