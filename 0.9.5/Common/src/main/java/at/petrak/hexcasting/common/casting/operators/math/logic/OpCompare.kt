package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import java.util.function.BiPredicate

class OpCompare(val acceptsEqual: Boolean, val cmp: BiPredicate<Double, Double>) : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = args.getChecked<Double>(0, argc)
        val rhs = args.getChecked<Double>(1, argc)
        if (lhs.tolerantEquals(rhs))
            return acceptsEqual.asSpellResult

        return cmp.test(lhs, rhs).asSpellResult
    }
}
