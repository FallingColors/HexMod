package at.petrak.hexcasting.common.casting.actions.math.logic

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.iota.Iota

object OpBoolIf : ConstMediaAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val cond = args.getBool(0, argc)
        val t = args[1]
        val f = args[2]
        return listOf(if (cond) t else f)
    }
}
