package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3
import kotlin.math.acos
import kotlin.math.clamp
import kotlin.math.sin

object OpVectorSlerp : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val a = args.getVec3(0, argc)
        val b = args.getVec3(1, argc)
        val t = clamp(args.getDouble(2, argc), 0.0, 1.0)

        val lenA = a.length()
        val lenB = b.length()
        val interpLen = lenA + (lenB - lenA) * t

        if (lenA == 0.0 || lenB == 0.0) {
            val res = a.scale(1.0 - t).add(b.scale(t))
            return res.asActionResult
        }

        val dot = a.dot(b)
        val cosTheta = clamp(dot / (lenA * lenB), -1.0, 1.0)
        val theta = acos(cosTheta)

        val resultVec: Vec3
        if (theta < 1e-6 || theta.isNaN()) {
            resultVec = a.scale(1.0 - t).add(b.scale(t))
        } else {
            val sinTheta = sin(theta)
            val weightA = sin((1.0 - t) * theta) / sinTheta
            val weightB = sin(t * theta) / sinTheta
            val unitDirA = a.scale(1.0 / lenA)
            val unitDirB = b.scale(1.0 / lenB)
            val slerpDir = unitDirA.scale(weightA).add(unitDirB.scale(weightB))
            resultVec = slerpDir.scale(interpLen)
        }
        return resultVec.asActionResult
    }
}
