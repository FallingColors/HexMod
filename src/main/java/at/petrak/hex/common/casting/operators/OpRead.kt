package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.Widget
import at.petrak.hex.common.items.ItemDataHolder

object OpRead : ConstManaOperator {
    override val argc = 0
    override val manaCost = 10

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val dataer = ctx.getDataHolder()
        val datum = (dataer.item as ItemDataHolder).readDatum(dataer.orCreateTag, ctx)
        return listOf(datum ?: SpellDatum.make(Widget.NULL))
    }
}