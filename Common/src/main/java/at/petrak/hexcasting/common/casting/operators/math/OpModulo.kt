package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getDouble
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero

object OpModulo : ConstManaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        // TODO: some wAckY vector operation to go in the vector x vector overload
        val l = args.getDouble(0, argc)
        val r = args.getDouble(1, argc)
        if (r == 0.0)
            throw MishapDivideByZero.of(args[0], args[1])
        return (l % r).asActionResult
    }
}
