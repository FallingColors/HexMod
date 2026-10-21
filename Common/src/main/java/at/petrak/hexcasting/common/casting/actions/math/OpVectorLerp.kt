package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import kotlin.math.clamp

object OpVectorLerp : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val a = args.getVec3(0, argc)
        val b = args.getVec3(1, argc)
        val t = clamp(args.getDouble(2, argc), 0.0, 1.0)

        val res = a.scale(1.0 - t).add(b.scale(t))
        return res.asActionResult
    }
}
