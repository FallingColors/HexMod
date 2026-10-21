package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota

object OpVectorCatmullRom : ConstMediaAction {
    override val argc: Int
        get() = 5

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val p0 = args.getVec3(0, argc)
        val p1 = args.getVec3(1, argc)
        val p2 = args.getVec3(2, argc)
        val p3 = args.getVec3(3, argc)
        val t = args.getDouble(4, argc)

        val tt = t * t
        val ttt = tt * t

        // Standard uniform Catmull-Rom spline formula between P1 and P2:
        // 0.5 * ((2 * P1) + (-P0 + P2) * t + (2*P0 - 5*P1 + 4*P2 - P3) * tt + (-P0 + 3*P1 - 3*P2 + P3) * ttt)
        val term0 = p1.scale(2.0)
        val term1 = p2.subtract(p0).scale(t)
        val term2 = p0.scale(2.0).subtract(p1.scale(5.0)).add(p2.scale(4.0)).subtract(p3).scale(tt)
        val term3 = p0.inverse().add(p1.scale(3.0)).subtract(p2.scale(3.0)).add(p3).scale(ttt)

        val res = term0.add(term1).add(term2).add(term3).scale(0.5)
        return res.asActionResult
    }
}
