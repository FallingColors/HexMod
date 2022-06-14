package at.petrak.hexcasting.common.casting.operators.math.trig

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getDouble
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero
import kotlin.math.cos
import kotlin.math.tan

object OpTan : ConstManaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val angle = args.getDouble(0, argc)
        if (cos(angle) == 0.0)
            throw MishapDivideByZero.tan(args[0] as DoubleIota)
        return tan(angle).asActionResult
    }
}
