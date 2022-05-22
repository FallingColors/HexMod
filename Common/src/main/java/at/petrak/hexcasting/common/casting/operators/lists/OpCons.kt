package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpCons : ConstManaOperator {
    override val argc = 2
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val bottom = args.getChecked<SpellList>(0, argc)
        val top = args[1]
        return SpellList.LPair(top, bottom).asSpellResult
    }
}
