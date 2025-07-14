package at.petrak.hexcasting.common.casting.actions.lists

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpSingleton : ConstMediaAction {
    override val argc = 1
    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        return Vector.from(listOf(args[0])).asActionResult // god i love one-liners
    }
}
