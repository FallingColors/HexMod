package at.petrak.hexcasting.common.casting.operators.math.trig

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getDouble
import at.petrak.hexcasting.api.spell.iota.Iota
import kotlin.math.atan2

object OpArcTan2 : ConstManaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val y = args.getDouble(0, argc)
        val x = args.getDouble(1, argc)
        return atan2(y, x).asActionResult
    }
}
