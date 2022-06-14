package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.spellListOf

object OpDuplicate : ConstManaOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val datum = args.getChecked<Any>(0, argc)
        return spellListOf(datum, datum)
    }
}
