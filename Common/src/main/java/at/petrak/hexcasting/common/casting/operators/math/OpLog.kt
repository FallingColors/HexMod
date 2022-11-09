package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getDouble
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero
import kotlin.math.log

object OpLog : ConstManaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val value = args.getDouble(0, argc)
        val base = args.getDouble(1, argc)
        if (value <= 0.0 || base <= 0.0 || base == 1.0)
            throw MishapDivideByZero.of(args[0], args[1], "logarithm")
        return log(value, base).asActionResult
    }
}
