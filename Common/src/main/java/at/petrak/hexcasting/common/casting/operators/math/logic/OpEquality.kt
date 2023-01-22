package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota

class OpEquality(val invert: Boolean) : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val lhs = args[0]
        val rhs = args[1]

        return (Iota.tolerates(lhs, rhs) != invert).asActionResult
    }
}
