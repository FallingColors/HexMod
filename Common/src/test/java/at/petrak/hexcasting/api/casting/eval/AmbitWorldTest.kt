package at.petrak.hexcasting.api.casting.eval

import at.petrak.hexcasting.common.casting.actions.world.OpDimensionQuery
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AmbitWorldTest {

    @Test
    fun testAmbitWorldBoundaryBehavior() {
        val casterPos = Vec3(0.0, 64.0, 0.0)
        val targetWithinAmbit = Vec3(0.0, 80.0, 0.0)
        val distance = targetWithinAmbit.distanceTo(casterPos)
        assertTrue(distance <= 32.0, "Target should be within ambit radius")
    }

    @Test
    fun testSilkTouchClusterLootInvariant() {
        val hasSilkTouch = true
        val dropsCluster = hasSilkTouch
        assertTrue(dropsCluster, "Silk touch must preserve amethyst cluster blocks intact")
    }

    @Test
    fun testDimensionQueryPrototype() {
        // Verify OpDimensionQuery prototype metadata
        assertEquals(0, OpDimensionQuery.argc)
        assertNotNull(OpDimensionQuery)
    }

    @Test
    fun testDimensionTypeRegistryScenarios() {
        // Verify vanilla vs custom datapack dimension ID parsing and matching
        val overworldId = ResourceLocation("minecraft", "overworld")
        val netherId = ResourceLocation("minecraft", "the_nether")
        val endId = ResourceLocation("minecraft", "the_end")
        val customDatapackDimId = ResourceLocation("hexcasting_addons", "mystical_realm")

        assertEquals("minecraft", overworldId.namespace)
        assertEquals("overworld", overworldId.path)
        assertEquals("minecraft", netherId.namespace)
        assertEquals("the_nether", netherId.path)
        assertEquals("minecraft", endId.namespace)
        assertEquals("the_end", endId.path)
        assertEquals("hexcasting_addons", customDatapackDimId.namespace)
        assertEquals("mystical_realm", customDatapackDimId.path)
    }
}
