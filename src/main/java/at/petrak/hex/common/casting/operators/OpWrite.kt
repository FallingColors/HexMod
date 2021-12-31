package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.items.ItemDataHolder

object OpWrite : ConstManaOperator {
    override val argc = 1
    override val manaCost = 10

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val dataer = ctx.getDataHolder()
        (dataer.item as ItemDataHolder).writeDatum(dataer.orCreateTag, args[0])
        return spellListOf()
    }
}