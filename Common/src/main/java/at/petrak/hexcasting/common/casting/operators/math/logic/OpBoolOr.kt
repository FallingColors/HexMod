package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBool
import at.petrak.hexcasting.api.spell.iota.Iota

object OpBoolOr : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val lhs = args.getBool(0, argc)
        val rhs = args.getBool(1, argc)
        return (lhs || rhs).asActionResult
    }
}
