package at.petrak.hexcasting.api.casting.eval.sideeffects

import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.Util
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack

/**
 * Things that happen after a spell is cast.
 */
sealed class OperatorSideEffect {
    /** Return whether to cancel all further [OperatorSideEffect] */
    abstract fun performEffect(harness: CastingVM): Boolean

    data class RequiredEnlightenment(val awardStat: Boolean) : OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Boolean {
            harness.env.caster?.sendSystemMessage("hexcasting.message.cant_great_spell".asTranslatedComponent)

            if (awardStat)
                HexAdvancementTriggers.FAIL_GREAT_SPELL_TRIGGER.trigger(harness.env.caster)

            return true
        }
    }

    /** Try to cast a spell  */
    data class AttemptSpell(
        val spell: RenderedSpell,
        val hasCastingSound: Boolean = true,
        val awardStat: Boolean = true
    ) :
        OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Boolean {
            this.spell.cast(harness.env)
            if (awardStat)
                harness.env.caster?.awardStat(HexStatistics.SPELLS_CAST)

            return false
        }
    }

    data class ConsumeMedia(val amount: Int) : OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Boolean {
            val leftoverMedia = harness.env.extractMedia(this.amount.toLong())
            return leftoverMedia > 0
        }
    }

    data class Particles(val spray: ParticleSpray) : OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Boolean {
            harness.env.produceParticles(this.spray, harness.env.colorizer)
//            this.spray.sprayParticles(harness.env.world, harness.env.colorizer)

            return false
        }
    }

    data class DoMishap(val mishap: Mishap, val errorCtx: Mishap.Context) : OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Boolean {
            val spray = mishap.particleSpray(harness.env)
            val color = mishap.accentColor(harness.env, errorCtx)
            spray.sprayParticles(harness.env.world, color)
            spray.sprayParticles(
                harness.env.world,
                FrozenPigment(
                    ItemStack(HexItems.DYE_COLORIZERS[DyeColor.RED]!!),
                    Util.NIL_UUID
                )
            )

            mishap.execute(harness.env, errorCtx, harness.image.stack.toMutableList())

            return true
        }
    }
}
