package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

object OpRandom : ConstMediaAction {
    override val argc: Int
        get() = 0

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        return env.world.random.nextDouble().asActionResult
    }
}
