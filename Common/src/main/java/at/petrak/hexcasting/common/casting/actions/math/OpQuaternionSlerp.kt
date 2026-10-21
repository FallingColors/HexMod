package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3
import kotlin.math.*

object OpQuaternionSlerp : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val q1 = args.getVec3(0, argc)
        val q2 = args.getVec3(1, argc)
        var t = args.getDouble(2, argc)
        t = t.coerceIn(0.0, 1.0)

        // Using Vec3 for 3 vector components and treating a 4th component or scaling
        // For full quaternion representation (w, x, y, z), we can pack/unpack or use extended types.
        // Here we implement standard spherical linear interpolation for 3D vectors / directional quaternions.
        val dot = (q1.x * q2.x + q1.y * q2.y + q1.z * q2.z).coerceIn(-1.0, 1.0)

        val theta = acos(dot)
        val sinTheta = sin(theta)

        if (sinTheta < 0.001) {
            // Linear interpolation fallback if angle is very small
            val interpolated = q1.scale(1.0 - t).add(q2.scale(t))
            return interpolated.asActionResult
        }

        val weight1 = sin((1.0 - t) * theta) / sinTheta
        val weight2 = sin(t * theta) / sinTheta

        val result = q1.scale(weight1).add(q2.scale(weight2))
        return result.asActionResult
    }
}
