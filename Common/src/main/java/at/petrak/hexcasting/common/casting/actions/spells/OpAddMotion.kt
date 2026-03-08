package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.components.CastingImageComponents
import at.petrak.hexcasting.api.casting.eval.vm.components.ImpulseScalingComponent
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
    const val MAX_MOTION: Double = 8192.0

    override fun executeWithImage(args: List<Iota>, env: CastingEnvironment, image: CastingImage): SpellAction.Result {
        val target = args.getEntity(0, argc)
        val motion = args.getVec3(1, argc)
        env.assertEntityInRange(target)

        var motionForCost = motion.lengthSqr()
        val updatedImpulseComponent = image.getComponent(CastingImageComponents.IMPULSE_SCALING) ?: ImpulseScalingComponent(HashSet())
        if (updatedImpulseComponent.impulsedEntities.contains(target.uuid))
            motionForCost++
        else
            updatedImpulseComponent.impulsedEntities.add(target.uuid)

        val shrunkMotion = if (motion.lengthSqr() > MAX_MOTION * MAX_MOTION)
            motion.normalize().scale(MAX_MOTION)
        else
            motion
        return SpellAction.Result(
            Spell(target, shrunkMotion, updatedImpulseComponent),
            (motionForCost * MediaConstants.DUST_UNIT).toLong(),
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

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        throw IllegalStateException()
    }

    private data class Spell(val target: Entity, val motion: Vec3, val newImpulseComponent: ImpulseScalingComponent) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {}
        override fun cast(env: CastingEnvironment, image: CastingImage): CastingImage {
            target.push(motion.x, motion.y, motion.z)
            target.hurtMarked = true // Whyyyyy
            return image.withComponent(CastingImageComponents.IMPULSE_SCALING, newImpulseComponent)
        }
    }
}
