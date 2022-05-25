package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpEmptyList : ConstManaOperator {
    override val argc = 0
    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        return emptyList<LegacySpellDatum<*>>().asSpellResult // sorry for taking all the easy impls, hudeler
    }
}
