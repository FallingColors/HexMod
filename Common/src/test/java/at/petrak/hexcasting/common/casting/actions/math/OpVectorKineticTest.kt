package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OpVectorKineticTest {

    @Test
    fun testVectorClampMagnitude() {
        val v = Vec3(3.0, 4.0, 0.0) // length = 5.0
        val clampedArgs = listOf(Vec3Iota(v), DoubleIota(2.5))
        val res = OpVectorClampMagnitude.execute(clampedArgs, null!!)
        assertEquals(1, res.size)
        val resVec = (res[0] as Vec3Iota).vec3
        assertEquals(1.5, resVec.x, 1e-5)
        assertEquals(2.0, resVec.y, 1e-5)
        assertEquals(0.0, resVec.z, 1e-5)
        assertEquals(2.5, resVec.length(), 1e-5)

        // Under max length
        val smallV = Vec3(1.0, 0.0, 0.0)
        val smallArgs = listOf(Vec3Iota(smallV), DoubleIota(5.0))
        val smallRes = OpVectorClampMagnitude.execute(smallArgs, null!!)
        val smallResVec = (smallRes[0] as Vec3Iota).vec3
        assertEquals(smallV, smallResVec)
    }

    @Test
    fun testVectorBezier() {
        val p0 = Vec3(0.0, 0.0, 0.0)
        val p1 = Vec3(0.0, 10.0, 0.0)
        val p2 = Vec3(10.0, 10.0, 0.0)
        val p3 = Vec3(10.0, 0.0, 0.0)

        // at t = 0 -> p0
        val res0 = OpVectorBezier.execute(listOf(Vec3Iota(p0), Vec3Iota(p1), Vec3Iota(p2), Vec3Iota(p3), DoubleIota(0.0)), null!!)
        assertEquals(p0, (res0[0] as Vec3Iota).vec3)

        // at t = 1 -> p3
        val res1 = OpVectorBezier.execute(listOf(Vec3Iota(p0), Vec3Iota(p1), Vec3Iota(p2), Vec3Iota(p3), DoubleIota(1.0)), null!!)
        assertEquals(p3, (res1[0] as Vec3Iota).vec3)

        // at t = 0.5
        val resHalf = OpVectorBezier.execute(listOf(Vec3Iota(p0), Vec3Iota(p1), Vec3Iota(p2), Vec3Iota(p3), DoubleIota(0.5)), null!!)
        val halfVec = (resHalf[0] as Vec3Iota).vec3
        // B(0.5) = 0.125*P0 + 0.375*P1 + 0.375*P2 + 0.125*P3 = (3.75, 7.5, 0.0)
        assertEquals(3.75, halfVec.x, 1e-5)
        assertEquals(7.5, halfVec.y, 1e-5)
        assertEquals(0.0, halfVec.z, 1e-5)
    }

    @Test
    fun testSmoothstep() {
        // Below edge0 (0.0) -> 0.0
        val resLow = OpSmoothstep.execute(listOf(DoubleIota(0.0), DoubleIota(1.0), DoubleIota(-0.5)), null!!)
        assertEquals(0.0, (resLow[0] as DoubleIota).double, 1e-5)

        // Above edge1 (1.0) -> 1.0
        val resHigh = OpSmoothstep.execute(listOf(DoubleIota(0.0), DoubleIota(1.0), DoubleIota(1.5)), null!!)
        assertEquals(1.0, (resHigh[0] as DoubleIota).double, 1e-5)

        // At midpoint 0.5 -> 0.5
        val resMid = OpSmoothstep.execute(listOf(DoubleIota(0.0), DoubleIota(1.0), DoubleIota(0.5)), null!!)
        assertEquals(0.5, (resMid[0] as DoubleIota).double, 1e-5)
    }
}
