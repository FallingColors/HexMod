package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota

object OpVectorBounce : ConstMediaAction {
    override val argc: Int
        get() = 4

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val v = args.getVec3(0, argc)
        val normal = args.getVec3(1, argc).normalize()
        val restitution = args.getDouble(2, argc)
        val friction = args.getDouble(3, argc)

        // Normal component: v_n = (v . n) * n
        val dot = v.dot(normal)
        val vNormal = normal.scale(dot)
        // Tangential component: v_t = v - v_n
        val vTangent = v.subtract(vNormal)

        // Reflected normal component with restitution (note: dot is negative for incoming towards normal)
        val reflectedNormal = vNormal.scale(-restitution)
        // Damped tangential component with friction
        val dampedTangent = vTangent.scale(kotlin.math.max(0.0, 1.0 - friction))

        val result = reflectedNormal.add(dampedTangent)
        return result.asActionResult
    }
}
