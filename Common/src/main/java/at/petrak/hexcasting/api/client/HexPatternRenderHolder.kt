package at.petrak.hexcasting.api.client

import at.petrak.hexcasting.api.casting.math.HexPattern
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.Vec3

data class HexPatternRenderHolder(val pattern: HexPattern, var lifetime: Int) {
    private var colourPos: Vec3? = null

    fun getColourPos(random: RandomSource): Vec3 {
        return colourPos ?: let {
            Vec3(random.nextDouble(), random.nextDouble(), random.nextDouble()).normalize().scale(3.0).also { colourPos = it }
        }
    }

    fun tick() {
        lifetime -= 1
    }
}