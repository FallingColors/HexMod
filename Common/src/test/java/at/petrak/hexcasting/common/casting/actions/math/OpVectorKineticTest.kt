package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
        assertEquals(3.75, halfVec.x, 1e-5)
        assertEquals(7.5, halfVec.y, 1e-5)
        assertEquals(0.0, halfVec.z, 1e-5)
    }

    @Test
    fun testSmoothstep() {
        val resLow = OpSmoothstep.execute(listOf(DoubleIota(0.0), DoubleIota(1.0), DoubleIota(-0.5)), null!!)
        assertEquals(0.0, (resLow[0] as DoubleIota).double, 1e-5)

        val resHigh = OpSmoothstep.execute(listOf(DoubleIota(0.0), DoubleIota(1.0), DoubleIota(1.5)), null!!)
        assertEquals(1.0, (resHigh[0] as DoubleIota).double, 1e-5)

        val resMid = OpSmoothstep.execute(listOf(DoubleIota(0.0), DoubleIota(1.0), DoubleIota(0.5)), null!!)
        assertEquals(0.5, (resMid[0] as DoubleIota).double, 1e-5)
    }

    @Test
    fun testVectorSpring() {
        val pos = Vec3(0.0, 0.0, 0.0)
        val vel = Vec3(1.0, 0.0, 0.0)
        val target = Vec3(10.0, 0.0, 0.0)
        val stiffness = 16.0
        val dt = 0.1

        val res = OpVectorSpring.execute(listOf(Vec3Iota(pos), Vec3Iota(vel), Vec3Iota(target), DoubleIota(stiffness), DoubleIota(dt)), null!!)
        assertEquals(2, res.size)
        val newPos = (res[0] as Vec3Iota).vec3
        assertEquals(true, newPos.x > 0.0)
    }

    @Test
    fun testVectorReflectNormal() {
        val v = Vec3(0.0, -1.0, 0.0)
        val normal = Vec3(0.0, 1.0, 0.0)
        val elasticity = 1.0

        val res = OpVectorReflectNormal.execute(listOf(Vec3Iota(v), Vec3Iota(normal), DoubleIota(elasticity)), null!!)
        assertEquals(1, res.size)
        val reflected = (res[0] as Vec3Iota).vec3
        assertEquals(0.0, reflected.x, 1e-5)
        assertEquals(1.0, reflected.y, 1e-5)
        assertEquals(0.0, reflected.z, 1e-5)
    }

    @Test
    fun testVectorCatmullRom() {
        val p0 = Vec3(0.0, 0.0, 0.0)
        val p1 = Vec3(1.0, 1.0, 0.0)
        val p2 = Vec3(2.0, 1.0, 0.0)
        val p3 = Vec3(3.0, 0.0, 0.0)

        // at t = 0 -> p1
        val res0 = OpVectorCatmullRom.execute(listOf(Vec3Iota(p0), Vec3Iota(p1), Vec3Iota(p2), Vec3Iota(p3), DoubleIota(0.0)), null!!)
        assertEquals(p1, (res0[0] as Vec3Iota).vec3)

        // at t = 1 -> p2
        val res1 = OpVectorCatmullRom.execute(listOf(Vec3Iota(p0), Vec3Iota(p1), Vec3Iota(p2), Vec3Iota(p3), DoubleIota(1.0)), null!!)
        assertEquals(p2, (res1[0] as Vec3Iota).vec3)
    }

    @Test
    fun testVectorHermiteSpline() {
        val p0 = Vec3(0.0, 0.0, 0.0)
        val t0 = Vec3(1.0, 0.0, 0.0)
        val p1 = Vec3(10.0, 0.0, 0.0)
        val t1 = Vec3(1.0, 0.0, 0.0)

        // at t = 0 -> p0
        val res0 = OpVectorHermiteSpline.execute(listOf(Vec3Iota(p0), Vec3Iota(t0), Vec3Iota(p1), Vec3Iota(t1), DoubleIota(0.0)), null!!)
        assertEquals(p0, (res0[0] as Vec3Iota).vec3)

        // at t = 1 -> p1
        val res1 = OpVectorHermiteSpline.execute(listOf(Vec3Iota(p0), Vec3Iota(t0), Vec3Iota(p1), Vec3Iota(t1), DoubleIota(1.0)), null!!)
        assertEquals(p1, (res1[0] as Vec3Iota).vec3)
    }

    @Test
    fun testVectorBounce() {
        val v = Vec3(1.0, -2.0, 0.0)
        val normal = Vec3(0.0, 1.0, 0.0)
        val restitution = 0.8
        val friction = 0.1

        val res = OpVectorBounce.execute(listOf(Vec3Iota(v), Vec3Iota(normal), DoubleIota(restitution), DoubleIota(friction)), null!!)
        assertEquals(1, res.size)
        val bounced = (res[0] as Vec3Iota).vec3
        assertEquals(0.9, bounced.x, 1e-5)
        assertEquals(1.6, bounced.y, 1e-5)
        assertEquals(0.0, bounced.z, 1e-5)
    }

    @Test
    fun testVectorReflect() {
        val v = Vec3(0.0, -5.0, 0.0)
        val normal = Vec3(0.0, 1.0, 0.0)
        val res = OpVectorReflect.execute(listOf(Vec3Iota(v), Vec3Iota(normal)), null!!)
        val reflected = (res[0] as Vec3Iota).vec3
        assertEquals(0.0, reflected.x, 1e-5)
        assertEquals(5.0, reflected.y, 1e-5)
        assertEquals(0.0, reflected.z, 1e-5)

        val vGrazing = Vec3(3.0, 0.0, 0.0)
        val resGrazing = OpVectorReflect.execute(listOf(Vec3Iota(vGrazing), Vec3Iota(normal)), null!!)
        val reflectedGrazing = (resGrazing[0] as Vec3Iota).vec3
        assertEquals(vGrazing, reflectedGrazing)

        assertThrows(MishapInvalidIota::class.java) {
            OpVectorReflect.execute(listOf(Vec3Iota(v), Vec3Iota(Vec3.ZERO)), null!!)
        }
    }

    @Test
    fun testVectorDampen() {
        val v = Vec3(10.0, 0.0, 0.0)
        val drag = 0.5
        val dt = 2.0

        val res = OpVectorDampen.execute(listOf(Vec3Iota(v), DoubleIota(drag), DoubleIota(dt)), null!!)
        assertEquals(1, res.size)
        val dampened = (res[0] as Vec3Iota).vec3
        // v * exp(-0.5 * 2.0) = v * exp(-1.0) ≈ 10.0 * 0.36787944 = 3.6787944
        assertEquals(10.0 * kotlin.math.exp(-1.0), dampened.x, 1e-5)
        assertEquals(0.0, dampened.y, 1e-5)
        assertEquals(0.0, dampened.z, 1e-5)

        // Zero dt -> energy preservation (identity)
        val resZeroDt = OpVectorDampen.execute(listOf(Vec3Iota(v), DoubleIota(drag), DoubleIota(0.0)), null!!)
        assertEquals(v, (resZeroDt[0] as Vec3Iota).vec3)
    }
}
