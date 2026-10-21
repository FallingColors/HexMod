package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.PI

class OpVectorGeometryTest {

    @Test
    fun testVectorAngle() {
        val a = Vec3(1.0, 0.0, 0.0)
        val b = Vec3(0.0, 1.0, 0.0)
        val res = OpVectorAngle.execute(listOf(Vec3Iota(a), Vec3Iota(b)), null)
        assertEquals(1, res.size)
        assertEquals(PI / 2.0, (res[0] as DoubleIota).double, 1e-5)

        // Colinear 0 degrees
        val resColinear0 = OpVectorAngle.execute(listOf(Vec3Iota(a), Vec3Iota(Vec3(2.0, 0.0, 0.0))), null)
        assertEquals(0.0, (resColinear0[0] as DoubleIota).double, 1e-5)

        // Colinear 180 degrees (pi)
        val resColinear180 = OpVectorAngle.execute(listOf(Vec3Iota(a), Vec3Iota(Vec3(-1.0, 0.0, 0.0))), null)
        assertEquals(PI, (resColinear180[0] as DoubleIota).double, 1e-5)

        // Zero vector fallback
        val zeroRes = OpVectorAngle.execute(listOf(Vec3Iota(Vec3.ZERO), Vec3Iota(b)), null)
        assertEquals(0.0, (zeroRes[0] as DoubleIota).double, 1e-5)
    }

    @Test
    fun testVectorSlerp() {
        val a = Vec3(1.0, 0.0, 0.0)
        val b = Vec3(0.0, 1.0, 0.0)
        val res = OpVectorSlerp.execute(listOf(Vec3Iota(a), Vec3Iota(b), DoubleIota(0.5)), null)
        assertEquals(1, res.size)
        val v = (res[0] as Vec3Iota).vec3
        assertEquals(v.x, v.y, 1e-5)
        assertEquals(0.0, v.z, 1e-5)

        // Antiparallel slerp fallback (180 degrees)
        val bAnti = Vec3(-1.0, 0.0, 0.0)
        val antiRes = OpVectorSlerp.execute(listOf(Vec3Iota(a), Vec3Iota(bAnti), DoubleIota(0.5)), null)
        assertEquals(1, antiRes.size)

        // Zero vector fallback in slerp
        val zeroSlerp = OpVectorSlerp.execute(listOf(Vec3Iota(Vec3.ZERO), Vec3Iota(b), DoubleIota(0.5)), null)
        assertEquals(1, zeroSlerp.size)
    }

    @Test
    fun testVectorLerp() {
        val a = Vec3(0.0, 0.0, 0.0)
        val b = Vec3(10.0, 20.0, 30.0)
        val res = OpVectorLerp.execute(listOf(Vec3Iota(a), Vec3Iota(b), DoubleIota(0.3)), null)
        assertEquals(1, res.size)
        val v = (res[0] as Vec3Iota).vec3
        assertEquals(3.0, v.x, 1e-5)
        assertEquals(6.0, v.y, 1e-5)
        assertEquals(9.0, v.z, 1e-5)

        // Negative factor / out of bounds factor clamping
        val clampRes = OpVectorLerp.execute(listOf(Vec3Iota(a), Vec3Iota(b), DoubleIota(-0.5)), null)
        assertEquals(1, clampRes.size)
        val vClamp = (clampRes[0] as Vec3Iota).vec3
        assertEquals(0.0, vClamp.x, 1e-5)
    }
}
