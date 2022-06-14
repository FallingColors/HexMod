package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getDouble
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.Iota
import java.util.function.BiPredicate

class OpCompare(val acceptsEqual: Boolean, val cmp: BiPredicate<Double, Double>) : ConstManaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val lhs = args.getDouble(0, argc)
        val rhs = args.getDouble(1, argc)
        if (DoubleIota.tolerates(lhs, rhs))
            return acceptsEqual.asActionResult

        return cmp.test(lhs, rhs).asActionResult
    }
}
