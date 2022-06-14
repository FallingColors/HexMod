package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions

class OpErase : SpellAction {
    override val argc = 0

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(it)
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

            (hexHolder?.hasHex() == true) ||
                    (datumHolder?.writeIota(null, true) == true)
        }
        val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)

        if ((hexHolder?.hasHex() != true) &&
            (datumHolder?.writeIota(null, true) != true)
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
                        (datumHolder?.writeIota(null, true) == true)
            }
            val hexHolder = IXplatAbstractions.INSTANCE.findHexHolder(handStack)
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)

            if (hexHolder?.hasHex() == true)
                hexHolder.clearHex()

            if (datumHolder != null && datumHolder.writeIota(null, true))
                datumHolder.writeIota(null, false)
        }
    }
}
