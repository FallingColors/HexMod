package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.cap.HexCapabilities
import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpReadable : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val (handStack) = ctx.getHeldItemToOperateOn {
            it.getCapability(HexCapabilities.DATUM).resolve().isPresent
        }

        val datumHolder = handStack.getCapability(HexCapabilities.DATUM).resolve()
        if (!datumHolder.isPresent)
            return spellListOf(0.0)

        if (datumHolder.get().readDatum(ctx.world) == null && datumHolder.get().emptyDatum() == null)
            return spellListOf(0.0)

        return spellListOf(1.0)
    }
}
