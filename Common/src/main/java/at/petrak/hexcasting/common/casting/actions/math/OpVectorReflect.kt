package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota

object OpVectorReflect : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val v = args.getVec3(0, argc)
        val normalRaw = args.getVec3(1, argc)
        val normalLenSqr = normalRaw.lengthSqr()
        if (normalLenSqr == 0.0) {
            throw MishapInvalidIota.of(args[1], 1, "non-zero vector")
        }
        val normal = normalRaw.scale(1.0 / kotlin.math.sqrt(normalLenSqr))

        // R = V - 2 * (V . N) * N
        val dot = v.dot(normal)
        val reflection = v.subtract(normal.scale(2.0 * dot))
        return reflection.asActionResult
    }
}
