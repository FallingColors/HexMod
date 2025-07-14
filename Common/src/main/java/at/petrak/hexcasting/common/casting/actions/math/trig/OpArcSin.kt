package at.petrak.hexcasting.common.casting.actions.math.trig

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDoubleBetween
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector
import kotlin.math.asin

object OpArcSin : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val value = args.getDoubleBetween(0, -1.0, 1.0, OpArcCos.argc)
        return asin(value).asActionResult
    }
}
