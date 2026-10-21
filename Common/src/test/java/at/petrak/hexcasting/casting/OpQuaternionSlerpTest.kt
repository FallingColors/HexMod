package at.petrak.hexcasting.casting

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.common.casting.actions.math.OpQuaternionSlerp
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OpQuaternionSlerpTest {
    @Test
    fun testSlerpMiddle() {
        val q1 = Vec3Iota(Vec3(1.0, 0.0, 0.0))
        val q2 = Vec3Iota(Vec3(0.0, 1.0, 0.0))
        val t = DoubleIota(0.5)

        val results = OpQuaternionSlerp.execute(listOf(q1, q2, t), null)
        assertEquals(1, results.size)
        val resVec = (results[0] as Vec3Iota).vec3
        assertEquals(0.707, resVec.x, 0.01)
        assertEquals(0.707, resVec.y, 0.01)
        assertEquals(0.0, resVec.z, 0.01)
    }
}
