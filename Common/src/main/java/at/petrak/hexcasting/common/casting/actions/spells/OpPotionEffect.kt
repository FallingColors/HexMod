package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity

class OpPotionEffect(
    val effect: MobEffect,
    val baseCost: Long,
    val allowPotency: Boolean,
    val potencyCubic: Boolean,
) : SpellAction {
    override val argc: Int
        get() = if (this.allowPotency) 3 else 2

    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getLivingEntityButNotArmorStand(0, argc)
        val duration = args.getPositiveDouble(1, argc)
        val potency = if (this.allowPotency)
            args.getDoubleBetween(2, 1.0, 127.0, argc)
        else 1.0
        env.assertEntityInRange(target)


        val cost = this.baseCost * duration * if (potencyCubic) {
            potency * potency * potency
        } else {
            potency * potency
        }
        return SpellAction.Result(
            Spell(effect, target, duration, potency),
            cost.toLong(),
            listOf(ParticleSpray.cloud(target.position().add(0.0, target.eyeHeight / 2.0, 0.0), 1.0))
        )
    }

    private class Spell(val effect: MobEffect, val target: LivingEntity, val duration: Double, val potency: Double) :
        RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (duration > 1.0 / 20.0) {
                val effectInst = MobEffectInstance(effect, (duration * 20).toInt(), potency.toInt() - 1)
                target.addEffect(effectInst)
            }
        }
    }
}
