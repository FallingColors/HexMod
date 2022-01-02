package at.petrak.hex.common.casting.operators.lists

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.Operator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum

object OpConcat : ConstManaOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = args.getChecked<List<SpellDatum<*>>>(0).toMutableList()
        val rhs = args.getChecked<List<SpellDatum<*>>>(1)
        lhs.addAll(rhs)
        return spellListOf(lhs)
    }
}