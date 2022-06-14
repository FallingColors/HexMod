package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero

object OpModulo : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val l = args.getChecked<Double>(0, argc)
        val r = args.getChecked<Double>(1, argc)
        if (r == 0.0)
            throw MishapDivideByZero.of(l, r)
        return (l % r).asSpellResult
    }
}
