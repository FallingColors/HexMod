package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota

object OpVectorClampMagnitude : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val v = args.getVec3(0, argc)
        val maxLength = args.getDouble(1, argc)

        val lenSqr = v.lengthSqr()
        val res = if (lenSqr > maxLength * maxLength && lenSqr > 0.0) {
            v.scale(maxLength / v.length())
        } else {
            v
        }
        return res.asActionResult
    }
}
