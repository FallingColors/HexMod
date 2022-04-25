package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import kotlin.math.max

class OpPotionEffect(
    val effect: MobEffect,
    val baseCost: Int,
    val allowPotency: Boolean,
    val potencyCubic: Boolean,
    val _isGreat: Boolean,
) : SpellOperator {
    override val argc: Int
        get() = if (this.allowPotency) 3 else 2
    override val isGreat = this._isGreat

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<LivingEntity>(0)
        if (target is ArmorStand)
            throw MishapInvalidIota.ofClass(SpellDatum.make(target), 0, LivingEntity::class.java)
        val duration = max(args.getChecked(1), 0.0)
        ctx.assertEntityInRange(target)
        val potency = if (this.allowPotency)
            max(args.getChecked(2), 1.0)
        else 1.0

        val cost = this.baseCost * duration * if (potencyCubic) {
            potency * potency * potency
        } else {
            potency * potency
        }
        return Triple(
            Spell(effect, target, duration, potency),
            cost.toInt(),
            listOf(ParticleSpray.Cloud(target.position().add(0.0, target.eyeHeight / 2.0, 0.0), 1.0))
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
