package at.petrak.hexcasting.casting

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.common.casting.actions.math.OpRaycastBoundingBox
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OpRaycastBoundingBoxTest {
    @Test
    fun testRaycastHit() {
        val origin = Vec3Iota(Vec3(0.0, 0.0, -5.0))
        val dir = Vec3Iota(Vec3(0.0, 0.0, 1.0))
        val minBox = Vec3Iota(Vec3(-1.0, -1.0, -1.0))
        val maxBox = Vec3Iota(Vec3(1.0, 1.0, 1.0))

        val results = OpRaycastBoundingBox.execute(listOf(origin, dir, minBox, maxBox), null)
        assertEquals(2, results.size)
        val dist = (results[0] as DoubleIota).double
        assertEquals(4.0, dist, 0.001)
    }
}
