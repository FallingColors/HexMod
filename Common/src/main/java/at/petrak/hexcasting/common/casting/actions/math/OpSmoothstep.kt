package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import kotlin.math.max
import kotlin.math.min

object OpSmoothstep : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val edge0 = args.getDouble(0, argc)
        val edge1 = args.getDouble(1, argc)
        val x = args.getDouble(2, argc)

        val t = if (edge0 == edge1) {
            if (x < edge0) 0.0 else 1.0
        } else {
            min(max((x - edge0) / (edge1 - edge0), 0.0), 1.0)
        }

        val res = t * t * (3.0 - 2.0 * t)
        return res.asActionResult
    }
}
