package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadEntity
import at.petrak.hexcasting.xplat.IXplatAbstractions

// we make this a spell cause imo it's a little ... anticlimactic for it to just make no noise
object OpTheCoolerWrite : SpellAction {
    override val argc = 1
    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val datum = args[0]
        val target = args.getEntity(1, argc)

        ctx.assertEntityInRange(target)

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(target)
            ?: throw MishapBadEntity.of(target, "iota.read")

        return Triple(
            Spell(datum, datumHolder),
            0,
            listOf()
        )
    }

    private data class Spell(val datum: Iota, val datumHolder: ADIotaHolder) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            datumHolder.writeIota(datum, false)
        }
    }
}
