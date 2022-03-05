package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.ParticleSpray
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.network.MsgBlinkAck
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.PacketDistributor

object OpTeleport : SpellOperator {
    override val argc = 2
    override val isGreat = true
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val teleportee = args.getChecked<Entity>(0)
        val delta = args.getChecked<Vec3>(1)
        ctx.assertVecInRange(teleportee.position())

        val targetMiddlePos = teleportee.position().add(0.0, teleportee.eyeHeight / 2.0, 0.0)

        return Triple(
            Spell(teleportee, delta),
            1_000_000,
            listOf(ParticleSpray.Cloud(targetMiddlePos, 2.0), ParticleSpray.Burst(targetMiddlePos.add(delta), 2.0))
        )
    }

    private data class Spell(val teleportee: Entity, val delta: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (delta.lengthSqr() < 32678.0 * 32678.0) {
                teleportee.setPos(teleportee.position().add(delta))
                if (teleportee is ServerPlayer) {
                    HexMessages.getNetwork().send(PacketDistributor.PLAYER.with { teleportee }, MsgBlinkAck(delta))
                }
            }

            if (teleportee is ServerPlayer) {
                teleportee.inventory.dropAll()
            }
        }

    }
}