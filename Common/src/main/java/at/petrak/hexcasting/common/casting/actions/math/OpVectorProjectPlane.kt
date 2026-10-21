package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3

object OpVectorProjectPlane : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val v = args.getVec3(0, argc)
        val p = args.getVec3(1, argc)
        val n = args.getVec3(2, argc)

        val lenSq = n.lengthSqr()
        if (lenSq < 1e-6) {
            return (v.subtract(p)).asActionResult
        }

        val unitN = n.scale(1.0 / n.length())
        val vp = v.subtract(p)
        val dot = vp.dot(unitN)
        val proj = vp.subtract(unitN.scale(dot))

        return proj.asActionResult
    }
}
