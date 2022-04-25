package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.cap.HexCapabilities
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem

object OpRead : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val datum = it.getCapability(HexCapabilities.DATUM).resolve()
            datum.isPresent && (datum.get().readDatum(ctx.world) != null || datum.get().emptyDatum() != null)
        }

        val datumHolder = handStack.getCapability(HexCapabilities.DATUM).resolve()
        if (!datumHolder.isPresent)
            throw MishapBadOffhandItem.of(handStack, hand, "iota.read")

        val datum = datumHolder.get().readDatum(ctx.world)
            ?: datumHolder.get().emptyDatum()
            ?: throw MishapBadOffhandItem.of(handStack, hand, "iota.read")

        return listOf(datum)
    }
}
