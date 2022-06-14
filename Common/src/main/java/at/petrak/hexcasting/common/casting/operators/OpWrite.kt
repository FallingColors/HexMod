package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.xplat.IXplatAbstractions

// we make this a spell cause imo it's a little ... anticlimactic for it to just make no noise
object OpWrite : SpellAction {
    override val argc = 1
    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val datum = args[0]

        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

            datumHolder != null && datumHolder.writeIota(datum, true)
        }

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: throw MishapBadOffhandItem.of(handStack, hand, "iota.write")

        if (!datumHolder.writeIota(datum, true))
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

    private data class Spell(val datum: Iota) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val (handStack) = ctx.getHeldItemToOperateOn {
                val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

                datumHolder != null && datumHolder.writeIota(datum, true)
            }

            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            datumHolder?.writeIota(datum, false)
        }
    }
}
