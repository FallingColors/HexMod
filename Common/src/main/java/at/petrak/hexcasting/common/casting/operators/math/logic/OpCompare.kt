package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import java.util.function.BiPredicate

class OpCompare(val cmp: BiPredicate<Double, Double>) : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = args.getChecked<Double>(0)
        val rhs = args.getChecked<Double>(1)
        return spellListOf(
            if (cmp.test(lhs, rhs)) 1.0 else 0.0
        )
    }
}
