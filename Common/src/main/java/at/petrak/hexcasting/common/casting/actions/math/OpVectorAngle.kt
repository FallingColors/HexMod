package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import kotlin.math.acos
import kotlin.math.clamp

object OpVectorAngle : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val a = args.getVec3(0, argc)
        val b = args.getVec3(1, argc)
        val lenA = a.length()
        val lenB = b.length()
        if (lenA == 0.0 || lenB == 0.0) {
            return 0.0.asActionResult
        }
        val dot = a.dot(b)
        val clamped = clamp(dot / (lenA * lenB), -1.0, 1.0)
        return acos(clamped).asActionResult
    }
}
