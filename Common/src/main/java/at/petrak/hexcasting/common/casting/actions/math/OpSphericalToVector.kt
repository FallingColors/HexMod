package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3
import kotlin.math.*

object OpSphericalToVector : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val sph = args.getVec3(0, argc)
        val r = sph.x
        val theta = sph.y
        val phi = sph.z

        val sinTheta = sin(theta)
        val x = r * sinTheta * cos(phi)
        val y = r * sinTheta * sin(phi)
        val z = r * cos(theta)

        return Vec3(x, y, z).asActionResult
    }
}
