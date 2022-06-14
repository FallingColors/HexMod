package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpEmptyList : ConstManaOperator {
    override val argc = 0
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return emptyList<Iota>().asSpellResult // sorry for taking all the easy impls, hudeler
    }
}
