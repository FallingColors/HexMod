package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadEntity
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpTheCoolerWrite : SpellAction {
    override val argc = 2
    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getEntity(0, argc)
        val datum = args[1]

        ctx.assertEntityInRange(target)

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(target)
            ?: throw MishapBadEntity.of(target, "iota.write")

        if (!datumHolder.writeIota(datum, true))
            throw MishapBadEntity.of(target, "iota.write")

        // We pass null here so that even the own caster won't be allowed into a focus.
        // Otherwise, you could sentinel scout to people and remotely write their names into things using a cleric circle.
        val trueName = MishapOthersName.getTrueNameFromDatum(datum, null)
        if (trueName != null)
            throw MishapOthersName(trueName)

        return Triple(
            Spell(datum, datumHolder),
            0,
            listOf(ParticleSpray.burst(target.position(), 0.5))
        )
    }

    private data class Spell(val datum: Iota, val datumHolder: ADIotaHolder) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            datumHolder.writeIota(datum, false)
        }
    }
}
