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
    val baseCost: Int,
    val allowPotency: Boolean,
    val potencyCubic: Boolean,
) : SpellAction {
    override val argc: Int
        get() = if (this.allowPotency) 3 else 2

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getLivingEntityButNotArmorStand(0, argc)
        val duration = args.getPositiveDouble(1, argc)
        val potency = if (this.allowPotency)
            args.getPositiveDoubleUnderInclusive(2, 127.0, argc)
        else 1.0
        ctx.assertEntityInRange(target)


        val cost = this.baseCost * duration * if (potencyCubic) {
            potency * potency * potency
        } else {
            potency * potency
        }
        return SpellAction.Result(
            Spell(effect, target, duration, potency),
            cost.toInt(),
            listOf(ParticleSpray.cloud(target.position().add(0.0, target.eyeHeight / 2.0, 0.0), 1.0))
        )
    }

    private class Spell(val effect: MobEffect, val target: LivingEntity, val duration: Double, val potency: Double) :
        RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            if (duration > 1.0 / 20.0) {
                val effectInst = MobEffectInstance(effect, (duration * 20).toInt(), potency.toInt() - 1)
                target.addEffect(effectInst)
            }
        }
    }
}
