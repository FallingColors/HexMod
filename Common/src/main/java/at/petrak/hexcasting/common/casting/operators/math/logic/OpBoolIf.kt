package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBool
import at.petrak.hexcasting.api.spell.iota.Iota

object OpBoolIf : ConstMediaAction {
    override val argc = 3

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val cond = args.getBool(0, argc)
        val t = args[1]
        val f = args[2]
        return listOf(if (cond) t else f)
    }
}
