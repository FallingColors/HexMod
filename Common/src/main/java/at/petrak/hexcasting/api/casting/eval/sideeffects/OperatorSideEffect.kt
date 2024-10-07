package at.petrak.hexcasting.api.casting.eval.sideeffects

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.casting.mishaps.MishapUnenlightened
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.common.lib.HexItems
import net.minecraft.Util
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import java.util.Optional

/**
 * Things that happen after a spell is cast.
 */
sealed class OperatorSideEffect {
    /** Return whether to cancel all further [OperatorSideEffect] */
    abstract fun performEffect(harness: CastingVM): Optional<DoMishap>

    data class RequiredEnlightenment(val awardStat: Boolean) : OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Optional<DoMishap> {
            harness.env.castingEntity?.sendSystemMessage("hexcasting.message.cant_great_spell".asTranslatedComponent)


            return Optional.of(DoMishap(MishapUnenlightened(), Mishap.Context(null, null)))
        }
    }

    /** Try to cast a spell  */
    data class AttemptSpell(
        val spell: RenderedSpell,
        val hasCastingSound: Boolean = true,
        val awardStat: Boolean = true
    ) :
        OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Optional<DoMishap> {
            this.spell.cast(harness.env, harness.image)?.let { harness.image = it }
            if (awardStat)
                (harness.env.castingEntity as? ServerPlayer)?.awardStat(HexStatistics.SPELLS_CAST)

            return Optional.empty()
        }
    }

    data class ConsumeMedia(val amount: Long) : OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Optional<DoMishap> {
            val leftoverMedia = harness.env.extractMedia(this.amount)
            return if (leftoverMedia > 0) {
                Optional.of(DoMishap(MishapNotEnoughMedia(), Mishap.Context(null, null)))
            } else {
                Optional.empty()
            }
        }
    }

    data class Particles(val spray: ParticleSpray) : OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Optional<DoMishap> {
            harness.env.produceParticles(this.spray, harness.env.pigment)
//            this.spray.sprayParticles(harness.env.world, harness.env.colorizer)

            return Optional.empty()
        }
    }

    data class DoMishap(val mishap: Mishap, val errorCtx: Mishap.Context) : OperatorSideEffect() {
        override fun performEffect(harness: CastingVM): Optional<DoMishap> {
            return Optional.of(this)
        }

        fun performMishap(harness: CastingVM) {
            val spray = mishap.particleSpray(harness.env)
            val color = mishap.accentColor(harness.env, errorCtx)
            spray.sprayParticles(harness.env.world, color)
            spray.sprayParticles(
                harness.env.world,
                FrozenPigment(
                    ItemStack(HexItems.DYE_PIGMENTS[DyeColor.RED]!!),
                    Util.NIL_UUID
                )
            )

            harness.image = harness.image.copy(stack = mishap.executeReturnStack(harness.env, errorCtx, harness.image.stack.toMutableList()))
        }
    }
}
