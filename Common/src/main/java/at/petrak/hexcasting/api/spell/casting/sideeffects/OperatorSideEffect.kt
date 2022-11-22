package at.petrak.hexcasting.api.spell.casting.sideeffects

import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.block.circle.BlockEntityAbstractImpetus
import at.petrak.hexcasting.api.misc.FrozenColorizer
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.mishaps.Mishap
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
    abstract fun performEffect(harness: CastingHarness): Boolean

    data class RequiredEnlightenment(val awardStat: Boolean) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            harness.ctx.caster.sendSystemMessage("hexcasting.message.cant_great_spell".asTranslatedComponent)

            if (awardStat)
                HexAdvancementTriggers.FAIL_GREAT_SPELL_TRIGGER.trigger(harness.ctx.caster)

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
        override fun performEffect(harness: CastingHarness): Boolean {
            this.spell.cast(harness.ctx)
            if (awardStat)
                harness.ctx.caster.awardStat(HexStatistics.SPELLS_CAST)

            return false
        }
    }

    data class ConsumeMedia(val amount: Int) : OperatorSideEffect() {
        override fun performEffect(harness: CastingHarness): Boolean {
            val overcastOk = harness.ctx.canOvercast
            val leftoverMedia = harness.withdrawMedia(this.amount, overcastOk)
            if (leftoverMedia > 0 && !overcastOk) {
                harness.ctx.caster.sendSystemMessage("hexcasting.message.cant_overcast".asTranslatedComponent)
            }
            return leftoverMedia > 0
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
                harness.ctx.caster.sendSystemMessage(msg)
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

            mishap.execute(harness.ctx, errorCtx, harness.stack)

            return true
        }
    }
}
