package at.petrak.hexcasting.common.casting.operators.math.bit

import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getLong
import at.petrak.hexcasting.api.casting.iota.Iota

object OpNot : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val num = args.getLong(0, argc)
        return num.inv().asActionResult
    }
}
