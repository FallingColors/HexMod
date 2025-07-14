package at.petrak.hexcasting.common.casting.actions.math.logic

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBool
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpBoolNot : ConstMediaAction {
    override val argc = 1

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val b = args.getBool(0, argc)
        return (!b).asActionResult
    }
}
