package at.petrak.hexcasting.common.casting.operators.math.trig

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import kotlin.math.sin

object OpSin : ConstManaOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val angle = args.getChecked<Double>(0, argc)
        return sin(angle).asSpellResult
    }
}
