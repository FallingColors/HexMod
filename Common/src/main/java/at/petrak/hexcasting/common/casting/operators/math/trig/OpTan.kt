package at.petrak.hexcasting.common.casting.operators.math.trig

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero
import kotlin.math.cos
import kotlin.math.tan

object OpTan : ConstManaOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val angle = args.getChecked<Double>(0, argc)
        if (cos(angle) == 0.0)
            throw MishapDivideByZero.tan(angle)
        return tan(angle).asSpellResult
    }
}
