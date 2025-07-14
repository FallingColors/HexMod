package at.petrak.hexcasting.common.casting.actions.math.trig

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector
import kotlin.math.atan2

object OpArcTan2 : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val y = args.getDouble(0, argc)
        val x = args.getDouble(1, argc)
        return atan2(y, x).asActionResult
    }
}
