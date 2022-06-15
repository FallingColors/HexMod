package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota

object OpBoolXor : ConstManaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val a = args[0]
        val b = args[1]

        return listOf(
            if (a.isTruthy && !b.isTruthy)
                a
            else if (!a.isTruthy && b.isTruthy)
                b
            else
                NullIota()
        )
    }
}
