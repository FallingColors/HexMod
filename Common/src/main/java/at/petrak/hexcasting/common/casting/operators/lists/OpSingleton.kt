package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpSingleton : ConstManaOperator {
    override val argc = 1
    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return listOf(args[0]).asSpellResult // god i love one-liners
    }
}
