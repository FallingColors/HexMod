package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity

class OpPotionEffect(
    val effect: MobEffect,
    val baseCost: Int,
    val allowPotency: Boolean,
    val potencyCubic: Boolean,
    override val isGreat: Boolean,
) : SpellAction {
    override val argc: Int
        get() = if (this.allowPotency) 3 else 2

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getLivingEntityButNotArmorStand(0, argc)
        val duration = args.getPositiveDouble(1, argc)
        val potency = if (this.allowPotency)
            args.getPositiveDoubleUnder(2, 127.0, argc)
        else 1.0
        ctx.assertEntityInRange(target)


        val cost = this.baseCost * duration * if (potencyCubic) {
            potency * potency * potency
        } else {
            potency * potency
        }
        return Triple(
            Spell(effect, target, duration, potency),
            cost.toInt(),
            listOf(ParticleSpray.cloud(target.position().add(0.0, target.eyeHeight / 2.0, 0.0), 1.0))
        )
    }

    private class Spell(val effect: MobEffect, val target: LivingEntity, val duration: Double, val potency: Double) :
        RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val effectInst = MobEffectInstance(effect, (duration * 20).toInt(), potency.toInt() - 1)
            target.addEffect(effectInst)
        }
    }
}
