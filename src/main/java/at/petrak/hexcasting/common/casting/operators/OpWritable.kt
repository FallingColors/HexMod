package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.cap.HexCapabilities
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName

object OpWritable : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val datum = args[0]

        val (handStack) = ctx.getHeldItemToOperateOn {
            val datumHolder = it.getCapability(HexCapabilities.DATUM).resolve()

            datumHolder.isPresent && datumHolder.get().writeDatum(datum, true)
        }

        val datumHolder = handStack.getCapability(HexCapabilities.DATUM).resolve()
        if (!datumHolder.isPresent)
            return spellListOf(0.0)

        if (!datumHolder.get().writeDatum(datum, true))
            return spellListOf(0.0)

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, ctx.caster)
        if (trueName != null)
            return spellListOf(0.0)

        return spellListOf(1.0)
    }
}
