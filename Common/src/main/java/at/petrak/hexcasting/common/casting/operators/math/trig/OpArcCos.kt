package at.petrak.hexcasting.common.casting.operators.math.trig

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getDoubleBetween
import at.petrak.hexcasting.api.spell.iota.Iota
import kotlin.math.acos

object OpArcCos : ConstManaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val value = args.getDoubleBetween(0, -1.0, 1.0, argc)
        return acos(value).asActionResult
    }
}
