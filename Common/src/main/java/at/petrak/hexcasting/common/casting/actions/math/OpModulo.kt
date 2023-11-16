package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapDivideByZero

object OpModulo : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        // TODO: some wAckY vector operation to go in the vector x vector overload
        val l = args.getDouble(0, argc)
        val r = args.getDouble(1, argc)
        if (r == 0.0)
            throw MishapDivideByZero.of(args[0], args[1])
        return (l % r).asActionResult
    }
}
