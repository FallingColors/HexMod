package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.mishaps.Mishap
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.Util
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack

/**
 * Things that happen after a spell is cast.
 */
sealed class OperatorSideEffect {
    /** Return whether to cancel all further [OperatorSideEffect] */
    abstract fun performEffect(harness: CastingHarness): Boolean

    data class RequiredEnlightenment(val awardStat: Boolean) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            harness.ctx.caster.sendMessage(
                "hexcasting.message.cant_great_spell".asTranslatedComponent,
                Util.NIL_UUID
            )

            if (awardStat)
                HexAdvancementTriggers.FAIL_GREAT_SPELL_TRIGGER.trigger(harness.ctx.caster)

            return true
        }
    }

    /** Try to cast a spell  */
    data class AttemptSpell(val spell: RenderedSpell, val hasCastingSound: Boolean = true, val awardStat: Boolean = true) :
        OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            this.spell.cast(harness.ctx)
            if (awardStat)
                harness.ctx.caster.awardStat(HexStatistics.SPELLS_CAST)
            return false
        }
    }

    data class ConsumeMana(val amount: Int) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            val overcastOk = harness.ctx.canOvercast
            val leftoverMana = harness.withdrawMana(this.amount, overcastOk)
            if (leftoverMana > 0 && !overcastOk) {
                harness.ctx.caster.sendMessage(
                    "hexcasting.message.cant_overcast".asTranslatedComponent,
                    Util.NIL_UUID
                )
            }
            return leftoverMana > 0
        }
    }

    data class Particles(val spray: ParticleSpray) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            this.spray.sprayParticles(harness.ctx.world, harness.getColorizer())

            return false
        }
    }

    data class DoMishap(val mishap: Mishap, val errorCtx: Mishap.Context) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            val msg = mishap.errorMessage(harness.ctx, errorCtx);
            if (harness.ctx.spellCircle != null) {
                val tile = harness.ctx.world.getBlockEntity(harness.ctx.spellCircle.impetusPos)
                if (tile is BlockEntityAbstractImpetus) {
                    tile.lastMishap = msg
                    tile.setChanged()
                }
            } else {
                // for now
                harness.ctx.caster.sendMessage(msg, Util.NIL_UUID)
            }

            val spray = mishap.particleSpray(harness.ctx)
            val color = mishap.accentColor(harness.ctx, errorCtx)
            spray.sprayParticles(harness.ctx.world, color)
            spray.sprayParticles(
                harness.ctx.world,
                FrozenColorizer(
                    ItemStack(HexItems.DYE_COLORIZERS[DyeColor.RED]!!),
                    Util.NIL_UUID
                )
            )

            harness.ctx.world.playSound(
                null, harness.ctx.position.x, harness.ctx.position.y, harness.ctx.position.z,
                HexSounds.FAIL_PATTERN, SoundSource.PLAYERS, 1f, 1f
            )

            mishap.execute(harness.ctx, errorCtx, harness.stack)

            return true
        }
    }
}
