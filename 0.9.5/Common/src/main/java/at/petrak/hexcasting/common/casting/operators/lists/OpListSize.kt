package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext

// it's still called beancounter's distillation in my heart
object OpListSize : ConstManaOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        return args.getChecked<SpellList>(0, argc).toList().size.asSpellResult // mmm one-liner
    }
}
