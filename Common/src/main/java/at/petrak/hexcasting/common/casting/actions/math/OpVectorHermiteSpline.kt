package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota

object OpVectorHermiteSpline : ConstMediaAction {
    override val argc: Int
        get() = 5

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val p0 = args.getVec3(0, argc)
        val t0 = args.getVec3(1, argc)
        val p1 = args.getVec3(2, argc)
        val t1 = args.getVec3(3, argc)
        val t = args.getDouble(4, argc)

        val tt = t * t
        val ttt = tt * t

        // Hermite basis functions:
        // h00(t) =  2t^3 - 3t^2 + 1
        // h10(t) =   t^3 - 2t^2 + t
        // h01(t) = -2t^3 + 3t^2
        // h11(t) =   t^3 -   t^2
        val h00 = 2.0 * ttt - 3.0 * tt + 1.0
        val h10 = ttt - 2.0 * tt + t
        val h01 = -2.0 * ttt + 3.0 * tt
        val h11 = ttt - tt

        val res = p0.scale(h00)
            .add(t0.scale(h10))
            .add(p1.scale(h01))
            .add(t1.scale(h11))

        return res.asActionResult
    }
}
