package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3

object OpVectorCrossNormalize : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val a = args.getVec3(0, argc)
        val b = args.getVec3(1, argc)
        val cross = a.cross(b)
        val len = cross.length()
        val result = if (len < 1e-6) {
            Vec3.ZERO
        } else {
            cross.scale(1.0 / len)
        }
        return result.asActionResult
    }
}
