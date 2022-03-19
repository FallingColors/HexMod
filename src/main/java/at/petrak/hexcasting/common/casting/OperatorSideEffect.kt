package at.petrak.hexcasting.common.casting

import at.petrak.hexcasting.api.ParticleSpray
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.lib.HexStatistics
import at.petrak.hexcasting.datagen.Advancements
import net.minecraft.Util
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.chat.TranslatableComponent
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Things that happen after a spell is cast.
 */
sealed class OperatorSideEffect {
    /** Return whether to cancel all further [OperatorSideEffect] */
    abstract fun performEffect(harness: CastingHarness): Boolean

    /** Try to cast a spell  */
    data class AttemptSpell(val spell: RenderedSpell, val isGreat: Boolean) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            return if (this.isGreat && !harness.ctx.isCasterEnlightened) {
                harness.ctx.caster.sendMessage(
                    TranslatableComponent("hexcasting.message.cant_great_spell"),
                    Util.NIL_UUID
                )
                Advancements.FAIL_GREAT_SPELL_TRIGGER.trigger(harness.ctx.caster)
                true
            } else {
                this.spell.cast(harness.ctx)
                harness.ctx.caster.awardStat(HexStatistics.SPELLS_CAST)
                false
            }
        }
    }

    data class ConsumeMana(val amount: Int) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            val overcastOk = harness.ctx.canOvercast
            val leftoverMana = harness.withdrawMana(this.amount, overcastOk)
            if (leftoverMana > 0 && !overcastOk) {
                harness.ctx.caster.sendMessage(
                    TranslatableComponent("hexcasting.message.cant_overcast"),
                    Util.NIL_UUID
                )
            }
            return leftoverMana > 0
        }
    }

    data class Particles(val spray: ParticleSpray) :
        OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            this.spray.sprayParticles(harness.ctx.world, harness.getColorizer())

            return false
        }
    }

    data class Mishap(val exn: CastException) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            harness.ctx.caster.sendMessage(
                TextComponent(exn.toString()),
                Util.NIL_UUID
            )

            when (exn.reason) {
                CastException.Reason.INVALID_PATTERN -> {
                    val idx = Random.nextInt(0..harness.stack.size)
                    harness.stack.add(idx, SpellDatum.make(Widget.GARBAGE))
                }
                else -> {}
            }

            return true
        }
    }
}