package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import at.petrak.hexcasting.common.items.ItemDataHolder

object OpRead : ConstManaOperator {
    override val argc = 0
    override val manaCost = 10

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val dataer = ctx.getDataHolder()
        val datum = (dataer.item as ItemDataHolder).readDatum(dataer.orCreateTag, ctx)
        return listOf(datum ?: SpellDatum.make(Widget.NULL))
    }
}