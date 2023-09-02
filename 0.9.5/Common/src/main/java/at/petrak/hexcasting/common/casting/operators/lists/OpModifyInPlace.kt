package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import kotlin.math.roundToInt

object OpModifyInPlace : ConstManaOperator {
    override val argc = 3
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val list = args.getChecked<SpellList>(0, argc)
        val index = args.getChecked<Double>(1, argc).roundToInt()
        val iota = args[2]
        return list.modifyAt(index) { SpellList.LPair(iota, it.cdr) }.asSpellResult
    }
}
