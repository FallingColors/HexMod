package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions

class OpErase : SpellOperator {
    override val argc = 0

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(it)
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

            (hexHolder?.hasHex() == true) ||
                    (datumHolder?.writeDatum(null, true) == true)
        }
        val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)

        if ((hexHolder?.getHex(ctx.world) == null) &&
            (datumHolder?.writeDatum(null, true) == false)
        ) {
            throw MishapBadOffhandItem.of(handStack, hand, "eraseable")
        }

        return Triple(
            Spell,
            ManaConstants.DUST_UNIT, listOf()
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(it)
                val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

                (hexHolder?.hasHex() == true) ||
                        (datumHolder?.writeDatum(null, true) == true)
            }
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)

            if (hexHolder?.hasHex() == true)
                hexHolder.clearHex()

            if (datumHolder != null && datumHolder.writeDatum(null, true))
                datumHolder.writeDatum(null, false)
        }
    }
}
