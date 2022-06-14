package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

object OpAddMotion : SpellAction {
    override val argc: Int
        get() = 2

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getEntity(0, argc)
        val motion = args.getVec3(1, argc)
        ctx.assertEntityInRange(target)
        var motionForCost = motion.lengthSqr()
        if (ctx.hasBeenGivenMotion(target))
            motionForCost++
        ctx.markEntityAsMotionAdded(target)
        return Triple(
            Spell(target, motion),
            (motionForCost * ManaConstants.DUST_UNIT).toInt(),
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
        override fun cast(ctx: CastingContext) {
            target.push(motion.x, motion.y, motion.z)
            target.hurtMarked = true // Whyyyyy
        }
    }
}
