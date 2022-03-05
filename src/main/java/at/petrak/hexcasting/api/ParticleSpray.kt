package at.petrak.hexcasting.api

import net.minecraft.world.phys.Vec3

data class ParticleSpray(val pos: Vec3, val vel: Vec3, val fuzziness: Double, val spread: Double) {
    companion object {
        @JvmStatic
        fun Burst(pos: Vec3, size: Double): ParticleSpray {
            return ParticleSpray(pos, Vec3(size, 0.0, 0.0), 0.0, 6.28)
        }

        @JvmStatic
        fun Cloud(pos: Vec3, size: Double): ParticleSpray {
            return ParticleSpray(pos, Vec3.ZERO, size, 0.0)
        }
    }
}