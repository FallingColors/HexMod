package at.petrak.hexcasting.common.casting.operators.math.bit

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpToSet : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        return args.getChecked<SpellList>(0, argc).toSet().toList().asSpellResult
    }
}
