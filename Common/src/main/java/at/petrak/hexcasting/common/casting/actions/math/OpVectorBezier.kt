package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota

object OpVectorBezier : ConstMediaAction {
    override val argc: Int
        get() = 5

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val p0 = args.getVec3(0, argc)
        val p1 = args.getVec3(1, argc)
        val p2 = args.getVec3(2, argc)
        val p3 = args.getVec3(3, argc)
        val t = args.getDouble(4, argc)

        val u = 1.0 - t
        val tt = t * t
        val uu = u * u
        val uuu = uu * u
        val ttt = tt * t

        val res = p0.scale(uuu)
            .add(p1.scale(3.0 * uu * t))
            .add(p2.scale(3.0 * u * tt))
            .add(p3.scale(ttt))

        return res.asActionResult
    }
}
