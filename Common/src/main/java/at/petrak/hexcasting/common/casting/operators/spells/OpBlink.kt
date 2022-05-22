package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapImmuneEntity
import at.petrak.hexcasting.common.network.MsgBlinkAck
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.roundToInt

object OpBlink : SpellOperator {
    override val argc = 2
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Entity>(0, argc)
        val delta = args.getChecked<Double>(1, argc)
        ctx.assertEntityInRange(target)

        if (!target.canChangeDimensions())
            throw MishapImmuneEntity(target)

        val dvec = targetDelta(ctx, target, delta)

        ctx.assertVecInRange(target.position())
        ctx.assertVecInRange(target.position().add(dvec))

        val targetMiddlePos = target.position().add(0.0, target.eyeHeight / 2.0, 0.0)

        return Triple(
            Spell(target, delta),
            ManaConstants.SHARD_UNIT * delta.roundToInt(),
            listOf(
                ParticleSpray.Cloud(targetMiddlePos, 2.0, 50),
                ParticleSpray.Burst(targetMiddlePos.add(dvec), 2.0, 100)
            )
        )
    }

    private data class Spell(val target: Entity, val delta: Double) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val dvec = targetDelta(ctx, target, delta)
            target.setPos(target.position().add(dvec))
            if (target is ServerPlayer) {
                target.connection.resetPosition()
                IXplatAbstractions.INSTANCE.sendPacketToPlayer(target, MsgBlinkAck(dvec))
            }
        }
    }

    private fun targetDelta(ctx: CastingContext, target: Entity, delta: Double): Vec3 {
        val look = target.lookAngle
        // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/entity/PieceTrickBlink.java#L74
        val dx = look.x * delta
        val dy = if (target != ctx.caster) {
            look.y * delta
        } else {
            max(0.0, look.y * delta)
        }
        val dz = look.z * delta

        return Vec3(dx, dy, dz)
    }
}
