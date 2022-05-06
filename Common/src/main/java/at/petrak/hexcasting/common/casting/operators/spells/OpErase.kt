package at.petrak.hexcasting.common.casting.operators.spells

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

            (hexHolder?.patterns != null) ||
                    (datumHolder?.writeDatum(null, true) ?: false)
        }
        val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)

        if ((hexHolder?.patterns == null) &&
            (datumHolder == null
                    || datumHolder.readDatum(ctx.world) == null
                    || !datumHolder.writeDatum(null, true))
        ) {
            throw MishapBadOffhandItem.of(handStack, hand, "eraseable")
        }

        return Triple(Spell, 10_000, listOf())
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(it)
                val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

                (hexHolder?.patterns != null) ||
                        (datumHolder?.writeDatum(null, true) ?: false)
            }
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            if (hexHolder?.patterns != null)
                hexHolder.clearPatterns()

            if (datumHolder != null && datumHolder.writeDatum(null, true))
                datumHolder.writeDatum(null, false)
        }
    }
}
