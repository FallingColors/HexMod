package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getEntity
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpAddMotion : SpellAction {
    override val argc: Int
        get() = 2

    // for bug #387
    val MAX_MOTION: Double = 8192.0

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getEntity(0, argc)
        val motion = args.getVec3(1, argc)
        ctx.assertEntityInRange(target)
        var motionForCost = motion.lengthSqr()
        if (ctx.hasBeenGivenMotion(target))
            motionForCost++
        ctx.markEntityAsMotionAdded(target)

        val shrunkMotion = if (motion.lengthSqr() > MAX_MOTION * MAX_MOTION)
            motion.normalize().scale(MAX_MOTION)
        else
            motion
        return Triple(
            Spell(target, shrunkMotion),
            (motionForCost * MediaConstants.DUST_UNIT).toInt(),
            listOf(
                ParticleSpray(
                    target.position().add(0.0, target.eyeHeight / 2.0, 0.0),
                    motion.normalize(),
                    0.0,
                    0.1
                )
            ),
        )
    }

    private data class Spell(val target: Entity, val motion: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            target.push(motion.x, motion.y, motion.z)
            target.hurtMarked = true // Whyyyyy
        }
    }
}
