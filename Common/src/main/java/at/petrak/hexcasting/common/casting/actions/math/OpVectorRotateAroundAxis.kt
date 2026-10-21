package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.getDouble
import net.minecraft.world.phys.Vec3
import kotlin.math.*

object OpVectorRotateAroundAxis : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val v = args.getVec3(0, argc)
        val axis = args.getVec3(1, argc).normalize()
        val angle = args.getDouble(2, argc)

        val cosT = cos(angle)
        val sinT = sin(angle)
        val dot = axis.dot(v)

        val cross = Vec3(
            axis.y * v.z - axis.z * v.y,
            axis.z * v.x - axis.x * v.z,
            axis.x * v.y - axis.y * v.x
        )

        val rotated = Vec3(
            v.x * cosT + cross.x * sinT + axis.x * dot * (1.0 - cosT),
            v.y * cosT + cross.y * sinT + axis.y * dot * (1.0 - cosT),
            v.z * cosT + cross.z * sinT + axis.z * dot * (1.0 - cosT)
        )

        return rotated.asActionResult
    }
}
