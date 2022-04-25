package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.cap.HexCapabilities
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName

// we make this a spell cause imo it's a little ... anticlimactic for it to just make no noise
object OpWrite : SpellOperator {
    override val argc = 1
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val datum = args[0]

        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val datumHolder = it.getCapability(HexCapabilities.DATUM).resolve()

            datumHolder.isPresent && datumHolder.get().writeDatum(datum, true)
        }

        val datumHolder = handStack.getCapability(HexCapabilities.DATUM).resolve()
        if (!datumHolder.isPresent)
            throw MishapBadOffhandItem.of(handStack, hand, "iota.write")

        if (!datumHolder.get().writeDatum(datum, true))
            throw MishapBadOffhandItem.of(handStack, hand, "iota.readonly", datum.display())

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, ctx.caster)
        if (trueName != null)
            throw MishapOthersName(trueName)

        return Triple(
            Spell(datum),
            0,
            listOf()
        )
    }

    private data class Spell(val datum: SpellDatum<*>) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val datumHolder = it.getCapability(HexCapabilities.DATUM).resolve()

                datumHolder.isPresent && datumHolder.get().writeDatum(datum, true)
            }

            val datumHolder = handStack.getCapability(HexCapabilities.DATUM).resolve()

            if (datumHolder.isPresent) {
                datumHolder.get().writeDatum(datum, false)
            }
        }

    }
}
