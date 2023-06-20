package at.petrak.hexcasting.api.utils

import org.joml.Vector3f
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

class QuaternionfUtilsTest {
    @Test
    fun testXYZ() {
        val random = Random(System.currentTimeMillis())

        repeat(500) {
            val expected = Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat())
            val output = QuaternionfUtils.fromXYZ(expected).toXYZ()
            assertTrue(expected.distanceSquared(output) < 0.000001)
                { "Expected distance between $expected and $output to be less than 0.000001, was ${expected.distanceSquared(output)}." }
        }
    }
}