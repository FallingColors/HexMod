package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpSplat : ConstManaOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> =
        args.getChecked<SpellList>(0, argc).toMutableList()
}
