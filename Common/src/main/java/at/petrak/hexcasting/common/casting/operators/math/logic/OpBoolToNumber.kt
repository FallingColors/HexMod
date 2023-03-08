package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.iota.Iota

object OpBoolToNumber : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val arg = args.getBool(0, argc)
        return (if (arg) 1.0 else 0.0).asActionResult
    }
}
