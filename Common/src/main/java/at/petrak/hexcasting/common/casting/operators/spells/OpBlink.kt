package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapImmuneEntity
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationTooFarAway
import at.petrak.hexcasting.common.casting.operators.spells.great.OpTeleport
import net.minecraft.world.entity.Entity
import kotlin.math.roundToInt

object OpBlink : SpellAction {
    override val argc = 2
    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getEntity(0, argc)
        val delta = args.getDouble(1, argc)
        ctx.assertEntityInRange(target)

        if (!target.canChangeDimensions() || target.type.`is`(HexTags.Entities.CANNOT_TELEPORT))
            throw MishapImmuneEntity(target)

        val dvec = target.lookAngle.scale(delta)
        val endPos = target.position().add(dvec)

        if (!HexConfig.server().canTeleportInThisDimension(ctx.world.dimension()))
            throw MishapLocationTooFarAway(endPos, "bad_dimension")

        ctx.assertVecInRange(target.position())
        ctx.assertVecInRange(endPos)
        if (!ctx.isVecInWorld(endPos.subtract(0.0, 1.0, 0.0)))
            throw MishapLocationTooFarAway(endPos, "too_close_to_out")


        val targetMiddlePos = target.position().add(0.0, target.eyeHeight / 2.0, 0.0)

        return Triple(
            Spell(target, delta),
            (MediaConstants.SHARD_UNIT * delta * 0.5).roundToInt(),
            listOf(
                ParticleSpray.cloud(targetMiddlePos, 2.0, 50),
                ParticleSpray.burst(targetMiddlePos.add(dvec), 2.0, 100)
            )
        )
    }

    private data class Spell(val target: Entity, val delta: Double) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            if (!HexConfig.server().canTeleportInThisDimension(ctx.world.dimension()))
                return

            val delta = target.lookAngle.scale(delta)
            OpTeleport.teleportRespectSticky(target, delta, ctx.world)
        }
    }
}
