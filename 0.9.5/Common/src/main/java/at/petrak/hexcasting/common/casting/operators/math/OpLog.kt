package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero
import kotlin.math.log

object OpLog : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val value = args.getChecked<Double>(0, argc)
        val base = args.getChecked<Double>(1, argc)
        if (value <= 0.0 || base <= 0.0 || base == 1.0)
            throw MishapDivideByZero.of(value, base, "logarithm")
        return log(value, base).asSpellResult
    }
}
