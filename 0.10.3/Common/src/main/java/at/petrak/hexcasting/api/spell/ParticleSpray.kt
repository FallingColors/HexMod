package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.common.network.MsgCastParticleAck
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

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

    fun sprayParticles(world: ServerLevel, color: FrozenColorizer) {
        IXplatAbstractions.INSTANCE.sendPacketNear(this.pos, 128.0, world, MsgCastParticleAck(this, color))
    }
}
