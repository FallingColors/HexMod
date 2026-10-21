package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3
import kotlin.math.*

object OpVectorToSpherical : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val v = args.getVec3(0, argc)
        val r = v.length()
        if (r == 0.0) {
            return Vec3(0.0, 0.0, 0.0).asActionResult
        }
        val theta = acos(v.z / r) // inclination from +Z (or polar angle)
        val phi = atan2(y = v.y, x = v.x) // azimuth in X-Y plane
        return Vec3(r, theta, phi).asActionResult
    }
}
