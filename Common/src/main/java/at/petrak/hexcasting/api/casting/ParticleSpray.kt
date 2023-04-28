package at.petrak.hexcasting.api.casting

import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.common.msgs.MsgCastParticleS2C
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

/**
 * @param fuzziness the radius of the sphere the particle might happen in (pos)
 * @param spread the max angle in radians the particle can move in, in relation to vel
 */
data class ParticleSpray(val pos: Vec3, val vel: Vec3, val fuzziness: Double, val spread: Double, val count: Int = 20) {
    companion object {
        @JvmStatic
        fun burst(pos: Vec3, size: Double, count: Int = 20): ParticleSpray {
            return ParticleSpray(pos, Vec3(size, 0.0, 0.0), 0.0, 3.14, count)
        }

        @JvmStatic
        fun cloud(pos: Vec3, size: Double, count: Int = 20): ParticleSpray {
            return ParticleSpray(pos, Vec3(0.0, 0.001, 0.0), size, 0.0, count)
        }
    }

    fun sprayParticles(world: ServerLevel, color: FrozenPigment) {
        IXplatAbstractions.INSTANCE.sendPacketNear(this.pos, 128.0, world, MsgCastParticleS2C(this, color))
    }
}
