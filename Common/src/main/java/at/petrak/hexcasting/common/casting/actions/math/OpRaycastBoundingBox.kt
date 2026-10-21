package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.min

object OpRaycastBoundingBox : ConstMediaAction {
    override val argc: Int
        get() = 4

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val origin = args.getVec3(0, argc)
        val dir = args.getVec3(1, argc).normalize()
        val minBox = args.getVec3(2, argc)
        val maxBox = args.getVec3(3, argc)

        // Slab method for AABB ray intersection
        var tMin = 0.0
        var tMax = Double.MAX_VALUE

        val invDir = Vec3(
            if (dir.x != 0.0) 1.0 / dir.x else Double.MAX_VALUE,
            if (dir.y != 0.0) 1.0 / dir.y else Double.MAX_VALUE,
            if (dir.z != 0.0) 1.0 / dir.z else Double.MAX_VALUE
        )

        // X slab
        val t1 = (minBox.x - origin.x) * invDir.x
        val t2 = (maxBox.x - origin.x) * invDir.x
        tMin = max(tMin, min(t1, t2))
        tMax = min(tMax, max(t1, t2))

        // Y slab
        val t3 = (minBox.y - origin.y) * invDir.y
        val t4 = (maxBox.y - origin.y) * invDir.y
        tMin = max(tMin, min(t3, t4))
        tMax = min(tMax, max(t3, t4))

        // Z slab
        val t5 = (minBox.z - origin.z) * invDir.z
        val t6 = (maxBox.z - origin.z) * invDir.z
        tMin = max(tMin, min(t5, t6))
        tMax = min(tMax, max(t5, t6))

        if (tMax >= tMin && tMax >= 0.0) {
            val hitDist = if (tMin >= 0.0) tMin else tMax
            val hitPoint = origin.add(dir.scale(hitDist))
            return listOf(hitDist.asActionResult[0], hitPoint.asActionResult[0])
        }

        return listOf(-1.0.asActionResult[0], Vec3.ZERO.asActionResult[0])
    }
}
