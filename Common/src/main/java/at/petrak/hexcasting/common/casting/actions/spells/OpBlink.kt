package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.casting.mishaps.MishapImmuneEntity
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.casting.actions.spells.great.OpTeleport
import net.minecraft.world.entity.Entity
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

object OpBlink : SpellAction {
    override val argc = 2
    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getEntity(0, argc)
        val delta = args.getDouble(1, argc)
        env.assertEntityInRange(target)

        if (!target.canChangeDimensions() || target.type.`is`(HexTags.Entities.CANNOT_TELEPORT))
            throw MishapImmuneEntity(target)

        val dvec = target.lookAngle.scale(delta)
        val endPos = target.position().add(dvec)

        if (!HexConfig.server().canTeleportInThisDimension(env.world.dimension()))
            throw MishapBadLocation(endPos, "bad_dimension")

        env.assertVecInRange(target.position())
        env.assertVecInRange(endPos)
        if (!env.isVecInWorld(endPos.subtract(0.0, 1.0, 0.0)))
            throw MishapBadLocation(endPos, "too_close_to_out")


        val targetMiddlePos = target.position().add(0.0, target.eyeHeight / 2.0, 0.0)

        return SpellAction.Result(
            Spell(target, delta),
            (MediaConstants.SHARD_UNIT * delta.absoluteValue * 0.5).roundToLong(),
            listOf(
                ParticleSpray.cloud(targetMiddlePos, 2.0, 50),
                ParticleSpray.burst(targetMiddlePos.add(dvec), 2.0, 100)
            )
        )
    }

    private data class Spell(val target: Entity, val delta: Double) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (!HexConfig.server().canTeleportInThisDimension(env.world.dimension()))
                return

            val delta = target.lookAngle.scale(delta)
            OpTeleport.teleportRespectSticky(target, delta, env.world)
        }
    }
}
