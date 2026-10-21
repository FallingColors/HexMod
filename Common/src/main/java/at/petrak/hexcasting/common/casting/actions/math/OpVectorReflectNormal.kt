package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota

object OpVectorReflectNormal : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val v = args.getVec3(0, argc)
        val normal = args.getVec3(1, argc).normalize()
        val elasticity = args.getDouble(2, argc)

        // R = V - (1 + elasticity) * (V . N) * N
        val dot = v.dot(normal)
        val reflection = v.subtract(normal.scale((1.0 + elasticity) * dot))
        return reflection.asActionResult
    }
}
