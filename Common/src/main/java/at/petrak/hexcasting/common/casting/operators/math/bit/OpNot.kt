package at.petrak.hexcasting.common.casting.operators.math.bit

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import kotlin.math.roundToInt

object OpNot : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val num = args.getChecked<Double>(0, argc).roundToInt()
        return num.inv().asSpellResult
    }
}
