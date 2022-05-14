package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.cap.HexCapabilities
import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem

class OpErase : SpellOperator {
    override val argc = 0

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val spellHolder = HexCapabilities.getCapability(it, HexCapabilities.SPELL)
            val datumHolder = HexCapabilities.getCapability(it, HexCapabilities.DATUM)

            (spellHolder.isPresent && spellHolder.get().hasSpell()) ||
                    (datumHolder.isPresent && datumHolder.get().writeDatum(null, true))
        }
        val spellHolder = HexCapabilities.getCapability(handStack, HexCapabilities.SPELL)
        val datumHolder = HexCapabilities.getCapability(handStack, HexCapabilities.DATUM)

        if ((!spellHolder.isPresent || !spellHolder.get().hasSpell()) &&
            (!datumHolder.isPresent || datumHolder.get().readDatum(ctx.world) == null ||
                    !datumHolder.get().writeDatum(null, true))) {
            throw MishapBadOffhandItem.of(handStack, hand, "eraseable")
        }

        return Triple(Spell, ManaConstants.DUST_UNIT, listOf())
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val spellHolder = HexCapabilities.getCapability(it, HexCapabilities.SPELL)
                val datumHolder = HexCapabilities.getCapability(it, HexCapabilities.DATUM)

                (spellHolder.isPresent && spellHolder.get().hasSpell()) ||
                        (datumHolder.isPresent && datumHolder.get().writeDatum(null, true))
            }
            val spellHolder = HexCapabilities.getCapability(handStack, HexCapabilities.SPELL)
            val datumHolder = HexCapabilities.getCapability(handStack, HexCapabilities.DATUM)

            if (spellHolder.isPresent && spellHolder.get().hasSpell())
                spellHolder.get().clearPatterns()

            if (datumHolder.isPresent && datumHolder.get().writeDatum(null, true))
                datumHolder.get().writeDatum(null, false)
        }
    }
}
