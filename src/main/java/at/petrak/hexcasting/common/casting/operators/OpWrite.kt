package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.items.ItemDataHolder

object OpWrite : ConstManaOperator {
    override val argc = 1
    override val manaCost = 10

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val dataer = ctx.getDataHolder()
        (dataer.item as ItemDataHolder).writeDatum(dataer.orCreateTag, args[0])
        return spellListOf()
    }
}